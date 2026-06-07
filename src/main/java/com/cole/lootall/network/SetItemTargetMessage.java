package com.cole.lootall.network;

import com.cole.lootall.server.StageGate;
import com.cole.lootall.server.TransferData;
import com.cole.lootall.server.TransferService;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SetItemTargetMessage implements IMessage {
    private ResourceLocation item;

    public SetItemTargetMessage() {
    }

    public SetItemTargetMessage(ResourceLocation item) {
        this.item = item;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        item = new ResourceLocation(new PacketBuffer(buf).readString(256));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeString(item.toString());
    }

    public static class Handler implements IMessageHandler<SetItemTargetMessage, IMessage> {
        @Override
        public IMessage onMessage(SetItemTargetMessage message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            final ResourceLocation itemId = message.item;
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
                    Item item = ForgeRegistries.ITEMS.getValue(itemId);
                    if (item == null || !TransferService.canTargetItem(player, item)) {
                        player.sendStatusMessage(new TextComponentTranslation("message.lootall.item_target_invalid"), true);
                        return;
                    }
                    TransferData.get(server).setItemTarget(player.getUniqueID(), itemId);
                    ITextComponent name = TransferService.itemTargetName(player, item);
                    player.sendStatusMessage(new TextComponentTranslation("message.lootall.target_set_item", name), true);
                }
            });
            return null;
        }
    }
}
