package com.cole.lootall.network;

import com.cole.lootall.server.TransferData;
import com.cole.lootall.server.TransferService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SetItemTargetPacket {
    private final ResourceLocation item;

    public SetItemTargetPacket(ResourceLocation item) {
        this.item = item;
    }

    public static void encode(SetItemTargetPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.item);
    }

    public static SetItemTargetPacket decode(FriendlyByteBuf buf) {
        return new SetItemTargetPacket(buf.readResourceLocation());
    }

    public static void handle(SetItemTargetPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(msg.item);
        if (!TransferService.canTargetItem(player, item)) {
            player.displayClientMessage(Component.translatable("message.lootall.item_target_invalid"), true);
            return;
        }
        TransferData.get(server).setItemTarget(player.getUUID(), msg.item);
        Component name = new ItemStack(item).getHoverName();
        player.displayClientMessage(Component.translatable("message.lootall.target_set_item", name), true);
    }
}
