package com.sxilverr.lootall.network;

import com.sxilverr.lootall.client.TransferFeedback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
//? if <1.21.1 {
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
//?}

public final class LootAllNetworkClient {

    private LootAllNetworkClient() {
    }

    //? if >=1.21.1 {
    /*public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(LootAllNetwork.LootFeedbackPayload.TYPE, (payload, context) ->
                TransferFeedback.show(payload.message(), payload.transfer().orElse(null)));
    }

    public static void sendLootAll() {
        ClientPlayNetworking.send(new LootAllNetwork.LootAllPayload());
    }

    public static void sendSetBlockTarget(BlockPos pos) {
        ClientPlayNetworking.send(new LootAllNetwork.SetBlockPayload(pos));
    }

    public static void sendClearTarget() {
        ClientPlayNetworking.send(new LootAllNetwork.ClearPayload());
    }

    public static void sendSetItemTarget(ResourceLocation item) {
        ClientPlayNetworking.send(new LootAllNetwork.SetItemPayload(item));
    }
    *///?} else {
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(LootAllNetwork.LOOT_FEEDBACK, (client, handler, buf, responseSender) -> {
            Component message = buf.readComponent();
            Component transfer = buf.readBoolean() ? buf.readComponent() : null;
            client.execute(() -> TransferFeedback.show(message, transfer));
        });
    }

    public static void sendLootAll() {
        ClientPlayNetworking.send(LootAllNetwork.LOOT_ALL, PacketByteBufs.create());
    }

    public static void sendSetBlockTarget(BlockPos pos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        ClientPlayNetworking.send(LootAllNetwork.SET_BLOCK_TARGET, buf);
    }

    public static void sendClearTarget() {
        ClientPlayNetworking.send(LootAllNetwork.CLEAR_TARGET, PacketByteBufs.create());
    }

    public static void sendSetItemTarget(ResourceLocation item) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeResourceLocation(item);
        ClientPlayNetworking.send(LootAllNetwork.SET_ITEM_TARGET, buf);
    }
    //?}
}
