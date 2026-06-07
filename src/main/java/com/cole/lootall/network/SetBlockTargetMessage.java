package com.cole.lootall.network;

import com.cole.lootall.server.StageGate;
import com.cole.lootall.server.TransferData;
import com.cole.lootall.server.TransferService;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetBlockTargetMessage implements IMessage {
    private BlockPos pos;

    public SetBlockTargetMessage() {
    }

    public SetBlockTargetMessage(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    public static class Handler implements IMessageHandler<SetBlockTargetMessage, IMessage> {
        @Override
        public IMessage onMessage(SetBlockTargetMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            final BlockPos pos = message.pos;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    MinecraftServer server = player.getServer();
                    if (server == null) {
                        return;
                    }
                    if (!StageGate.canTransfer(player)) {
                        player.sendStatusMessage(new TextComponentTranslation("message.lootall.no_stage"), true);
                        return;
                    }
                    if (!TransferService.canTargetBlock((WorldServer) player.world, pos)) {
                        player.sendStatusMessage(new TextComponentTranslation("message.lootall.target_invalid"), true);
                        return;
                    }
                    TransferData.get(server).setBlockTarget(player.getUniqueID(), player.dimension, pos);
                    ITextComponent name = TransferService.blockTargetName(player.world, pos);
                    player.sendStatusMessage(new TextComponentTranslation("message.lootall.target_set",
                            name, pos.getX(), pos.getY(), pos.getZ()), true);
                }
            });
            return null;
        }
    }
}
