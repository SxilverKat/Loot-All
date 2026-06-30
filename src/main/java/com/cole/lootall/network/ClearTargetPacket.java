package com.cole.lootall.network;

import com.cole.lootall.server.TransferData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ClearTargetPacket {
    public ClearTargetPacket() {
    }

    public static void encode(ClearTargetPacket msg, FriendlyByteBuf buf) {
    }

    public static ClearTargetPacket decode(FriendlyByteBuf buf) {
        return new ClearTargetPacket();
    }

    public static void handle(ClearTargetPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        TransferData.get(server).clear(player.getUUID());
        player.displayClientMessage(Component.translatable("message.lootall.target_cleared"), true);
    }
}
