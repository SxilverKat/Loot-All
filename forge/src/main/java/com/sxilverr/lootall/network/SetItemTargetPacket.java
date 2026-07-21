package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Text;
import com.sxilverr.lootall.server.StageGate;
import com.sxilverr.lootall.core.TransferData;
import com.sxilverr.lootall.server.TransferService;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >=1.17 {
import net.minecraftforge.network.NetworkEvent;
//?} else {
/*import net.minecraftforge.fml.network.NetworkEvent;*/
//?}

import java.util.function.Supplier;

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

    public static void handle(SetItemTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            MinecraftServer server = player.getServer();
            if (server == null) {
                return;
            }
            if (!StageGate.canTransfer(player)) {
                player.displayClientMessage(Text.translatable("message.lootall.no_stage"), true);
                return;
            }
            Item item = ForgeRegistries.ITEMS.getValue(msg.item);
            if (!TransferService.canTargetItem(player, item)) {
                player.displayClientMessage(Text.translatable("message.lootall.item_target_invalid"), true);
                return;
            }
            TransferData.get(server).setItemTarget(player.getUUID(), msg.item);
            Component name = new ItemStack(item).getHoverName();
            player.displayClientMessage(Text.translatable("message.lootall.target_set_item", name), true);
        });
        context.setPacketHandled(true);
    }
}
