package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.client.TransferFeedback;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record LootFeedbackPacket(Component message, Optional<Component> transfer) implements CustomPacketPayload {
    public static final Type<LootFeedbackPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Config.MOD_ID, "loot_feedback"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LootFeedbackPacket> CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, LootFeedbackPacket::message,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, LootFeedbackPacket::transfer,
            LootFeedbackPacket::new);

    @Override
    public Type<LootFeedbackPacket> type() {
        return TYPE;
    }

    public static void handle(LootFeedbackPacket msg, IPayloadContext ctx) {
        TransferFeedback.show(msg.message, msg.transfer.orElse(null));
    }
}
