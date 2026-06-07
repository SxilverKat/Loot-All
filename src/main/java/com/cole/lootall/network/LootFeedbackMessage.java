package com.cole.lootall.network;

import com.cole.lootall.LootAll;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LootFeedbackMessage implements IMessage {
    private ITextComponent message;
    private ITextComponent transfer;

    public LootFeedbackMessage() {
    }

    public LootFeedbackMessage(ITextComponent message, ITextComponent transfer) {
        this.message = message;
        this.transfer = transfer;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        message = ITextComponent.Serializer.jsonToComponent(pb.readString(32767));
        transfer = pb.readBoolean() ? ITextComponent.Serializer.jsonToComponent(pb.readString(32767)) : null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeString(ITextComponent.Serializer.componentToJson(message));
        pb.writeBoolean(transfer != null);
        if (transfer != null) {
            pb.writeString(ITextComponent.Serializer.componentToJson(transfer));
        }
    }

    public static class Handler implements IMessageHandler<LootFeedbackMessage, IMessage> {
        @Override
        public IMessage onMessage(LootFeedbackMessage message, MessageContext ctx) {
            LootAll.proxy.showFeedback(message.message, message.transfer);
            return null;
        }
    }
}
