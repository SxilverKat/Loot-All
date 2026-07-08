package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.core.TransferData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClearTargetPacket() implements CustomPacketPayload {
    public static final Type<ClearTargetPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Config.MOD_ID, "clear_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearTargetPacket> CODEC =
            StreamCodec.unit(new ClearTargetPacket());

    @Override
    public Type<ClearTargetPacket> type() {
        return TYPE;
    }

    public static void handle(ClearTargetPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
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
