package com.cole.lootall.network;

import com.cole.lootall.LootAll;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class LootAllNetwork {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(LootAll.MODID, "main"))
            .networkProtocolVersion(1)
            .simpleChannel();

    public static void register() {
        CHANNEL.messageBuilder(LootAllPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(LootAllPacket::encode)
                .decoder(LootAllPacket::decode)
                .consumerMainThread(LootAllPacket::handle)
                .add();
        CHANNEL.messageBuilder(SetBlockTargetPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetBlockTargetPacket::encode)
                .decoder(SetBlockTargetPacket::decode)
                .consumerMainThread(SetBlockTargetPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClearTargetPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ClearTargetPacket::encode)
                .decoder(ClearTargetPacket::decode)
                .consumerMainThread(ClearTargetPacket::handle)
                .add();
        CHANNEL.messageBuilder(SetItemTargetPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetItemTargetPacket::encode)
                .decoder(SetItemTargetPacket::decode)
                .consumerMainThread(SetItemTargetPacket::handle)
                .add();
    }
}
