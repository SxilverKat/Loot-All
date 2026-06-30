package com.cole.lootall.network;

import com.cole.lootall.server.LootAllHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class LootAllPacket {
    public LootAllPacket() {
    }

    public static void encode(LootAllPacket msg, FriendlyByteBuf buf) {
    }

    public static LootAllPacket decode(FriendlyByteBuf buf) {
        return new LootAllPacket();
    }

    public static void handle(LootAllPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            LootAllHandler.lootAll(player);
        }
    }
}
