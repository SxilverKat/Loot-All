package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class LootAllNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Config.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, LootAllPacket.class,
                LootAllPacket::encode, LootAllPacket::decode, LootAllPacket::handle);
        CHANNEL.registerMessage(1, SetBlockTargetPacket.class,
                SetBlockTargetPacket::encode, SetBlockTargetPacket::decode, SetBlockTargetPacket::handle);
        CHANNEL.registerMessage(2, ClearTargetPacket.class,
                ClearTargetPacket::encode, ClearTargetPacket::decode, ClearTargetPacket::handle);
        CHANNEL.registerMessage(3, SetItemTargetPacket.class,
                SetItemTargetPacket::encode, SetItemTargetPacket::decode, SetItemTargetPacket::handle);
        CHANNEL.registerMessage(4, LootFeedbackPacket.class,
                LootFeedbackPacket::encode, LootFeedbackPacket::decode, LootFeedbackPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(5, RefreshBlockPacket.class,
                RefreshBlockPacket::encode, RefreshBlockPacket::decode, RefreshBlockPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
