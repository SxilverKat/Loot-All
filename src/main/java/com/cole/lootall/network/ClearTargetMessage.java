package com.cole.lootall.network;

import com.cole.lootall.server.TransferData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClearTargetMessage implements IMessage {
    public ClearTargetMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<ClearTargetMessage, IMessage> {
        @Override
        public IMessage onMessage(ClearTargetMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    MinecraftServer server = player.getServer();
                    if (server == null) {
                        return;
                    }
                    TransferData.get(server).clear(player.getUniqueID());
                    player.sendStatusMessage(new TextComponentTranslation("message.lootall.target_cleared"), true);
                }
            });
            return null;
        }
    }
}
