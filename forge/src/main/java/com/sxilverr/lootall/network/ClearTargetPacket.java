package com.sxilverr.lootall.network;

import com.sxilverr.lootall.core.TransferData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearTargetPacket {
    public ClearTargetPacket() {
    }

    public static void encode(ClearTargetPacket msg, FriendlyByteBuf buf) {
    }

    public static ClearTargetPacket decode(FriendlyByteBuf buf) {
        return new ClearTargetPacket();
    }

    public static void handle(ClearTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
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
            TransferData.get(server).clear(player.getUUID());
            player.displayClientMessage(Component.translatable("message.lootall.target_cleared"), true);
        });
        context.setPacketHandled(true);
    }
}
