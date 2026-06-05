package com.cole.lootall.network;

import com.cole.lootall.server.StageGate;
import com.cole.lootall.server.TransferData;
import com.cole.lootall.server.TransferService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetBlockTargetPacket {
    private final BlockPos pos;

    public SetBlockTargetPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(SetBlockTargetPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static SetBlockTargetPacket decode(FriendlyByteBuf buf) {
        return new SetBlockTargetPacket(buf.readBlockPos());
    }

    public static void handle(SetBlockTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
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
                player.displayClientMessage(Component.translatable("message.lootall.no_stage"), true);
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
        });
        context.setPacketHandled(true);
    }
}
