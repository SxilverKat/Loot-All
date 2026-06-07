package com.cole.lootall.network;

import com.cole.lootall.server.LootAllHandler;
import com.cole.lootall.server.StageGate;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LootAllMessage implements IMessage {
    public LootAllMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<LootAllMessage, IMessage> {
        @Override
        public IMessage onMessage(LootAllMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (!StageGate.canLootAll(player)) {
                        player.sendStatusMessage(new TextComponentTranslation("message.lootall.no_stage"), true);
                        return;
                    }
                    LootAllHandler.lootAll(player);
                }
            });
            return null;
        }
    }
}
