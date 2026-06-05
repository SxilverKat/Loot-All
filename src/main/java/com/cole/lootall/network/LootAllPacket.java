package com.cole.lootall.network;

import com.cole.lootall.server.LootAllHandler;
import com.cole.lootall.server.StageGate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LootAllPacket {
    public LootAllPacket() {
    }

    public static void encode(LootAllPacket msg, FriendlyByteBuf buf) {
    }

    public static LootAllPacket decode(FriendlyByteBuf buf) {
        return new LootAllPacket();
    }

    public static void handle(LootAllPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (!StageGate.canLootAll(player)) {
                    player.displayClientMessage(Component.translatable("message.lootall.no_stage"), true);
                    return;
                }
                LootAllHandler.lootAll(player);
            }
        });
        context.setPacketHandled(true);
    }
}
