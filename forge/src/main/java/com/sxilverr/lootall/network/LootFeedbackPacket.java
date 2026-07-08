package com.sxilverr.lootall.network;

import com.sxilverr.lootall.client.TransferFeedback;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class LootFeedbackPacket {
    private final Component message;
    @Nullable
    private final Component transfer;

    public LootFeedbackPacket(Component message, @Nullable Component transfer) {
        this.message = message;
        this.transfer = transfer;
    }

    public static void encode(LootFeedbackPacket msg, FriendlyByteBuf buf) {
        buf.writeComponent(msg.message);
        buf.writeBoolean(msg.transfer != null);
        if (msg.transfer != null) {
            buf.writeComponent(msg.transfer);
        }
    }

    public static LootFeedbackPacket decode(FriendlyByteBuf buf) {
        Component message = buf.readComponent();
        Component transfer = buf.readBoolean() ? buf.readComponent() : null;
        return new LootFeedbackPacket(message, transfer);
    }

    public static void handle(LootFeedbackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> TransferFeedback.show(msg.message, msg.transfer)));
        context.setPacketHandled(true);
    }
}
