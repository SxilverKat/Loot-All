package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Compat;
import com.sxilverr.lootall.Text;
import com.sxilverr.lootall.server.StageGate;
import com.sxilverr.lootall.core.TransferData;
import com.sxilverr.lootall.server.TransferService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.17 {
import net.minecraftforge.network.NetworkEvent;
//?} else {
/*import net.minecraftforge.fml.network.NetworkEvent;*/
//?}

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
                player.displayClientMessage(Text.translatable("message.lootall.no_stage"), true);
                return;
            }
            if (!TransferService.canTargetBlock((ServerLevel) Compat.level(player), msg.pos)) {
                player.displayClientMessage(Text.translatable("message.lootall.target_invalid"), true);
                return;
            }
            TransferData.get(server).setBlockTarget(player.getUUID(), Compat.level(player).dimension(), msg.pos);
            Component name = Compat.level(player).getBlockState(msg.pos).getBlock().getName();
            player.displayClientMessage(Text.translatable(
                    "message.lootall.target_set", name, msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()), true);
        });
        context.setPacketHandled(true);
    }
}
