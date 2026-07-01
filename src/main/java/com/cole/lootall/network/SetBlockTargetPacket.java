package com.cole.lootall.network;

import com.cole.lootall.LootAll;
import com.cole.lootall.server.TransferData;
import com.cole.lootall.server.TransferService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetBlockTargetPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<SetBlockTargetPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LootAll.MODID, "set_block_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetBlockTargetPacket> CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, SetBlockTargetPacket::pos, SetBlockTargetPacket::new);

    @Override
    public Type<SetBlockTargetPacket> type() {
        return TYPE;
    }

    public static void handle(SetBlockTargetPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        if (!TransferService.canTargetBlock((ServerLevel) player.level(), msg.pos)) {
            player.displayClientMessage(Component.translatable("message.lootall.target_invalid"), true);
            return;
        }
        TransferData.get(server).setBlockTarget(player.getUUID(), player.level().dimension(), msg.pos);
        Component name = player.level().getBlockState(msg.pos).getBlock().getName();
        player.displayClientMessage(Component.translatable(
                "message.lootall.target_set", name, msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()), true);
    }
}
