package com.cole.lootall.network;

import com.cole.lootall.LootAll;
import com.cole.lootall.server.LootAllHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LootAllPacket() implements CustomPacketPayload {
    public static final Type<LootAllPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LootAll.MODID, "loot_all"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LootAllPacket> CODEC =
            StreamCodec.unit(new LootAllPacket());

    @Override
    public Type<LootAllPacket> type() {
        return TYPE;
    }

    public static void handle(LootAllPacket msg, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            LootAllHandler.lootAll(player);
        }
    }
}
