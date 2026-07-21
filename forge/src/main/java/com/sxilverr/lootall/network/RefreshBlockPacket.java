package com.sxilverr.lootall.network;

import com.sxilverr.lootall.client.ClientBlockRefresh;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
//? if >=1.17 {
import net.minecraftforge.network.NetworkEvent;
//?} else {
/*import net.minecraftforge.fml.network.NetworkEvent;*/
//?}

import java.util.function.Supplier;

public class RefreshBlockPacket {
    private final BlockPos pos;

    public RefreshBlockPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RefreshBlockPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static RefreshBlockPacket decode(FriendlyByteBuf buf) {
        return new RefreshBlockPacket(buf.readBlockPos());
    }

    public static void handle(RefreshBlockPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientBlockRefresh.refresh(msg.pos)));
        context.setPacketHandled(true);
    }
}
