package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Text;
import com.sxilverr.lootall.core.TransferData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.17 {
import net.minecraftforge.network.NetworkEvent;
//?} else {
/*import net.minecraftforge.fml.network.NetworkEvent;*/
//?}

import java.util.function.Supplier;

public class ClearTargetPacket {
    public ClearTargetPacket() {
    }

    public static void encode(ClearTargetPacket msg, FriendlyByteBuf buf) {
    }

    public static ClearTargetPacket decode(FriendlyByteBuf buf) {
        return new ClearTargetPacket();
    }

    public static void handle(ClearTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
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
            TransferData.get(server).clear(player.getUUID());
            player.displayClientMessage(Text.translatable("message.lootall.target_cleared"), true);
        });
        context.setPacketHandled(true);
    }
}
