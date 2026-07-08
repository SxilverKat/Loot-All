package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.core.TransferData;
import com.sxilverr.lootall.server.TransferService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetItemTargetPacket(ResourceLocation item) implements CustomPacketPayload {
    public static final Type<SetItemTargetPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Config.MOD_ID, "set_item_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetItemTargetPacket> CODEC =
            StreamCodec.composite(ResourceLocation.STREAM_CODEC, SetItemTargetPacket::item, SetItemTargetPacket::new);

    @Override
    public Type<SetItemTargetPacket> type() {
        return TYPE;
    }

    public static void handle(SetItemTargetPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
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
