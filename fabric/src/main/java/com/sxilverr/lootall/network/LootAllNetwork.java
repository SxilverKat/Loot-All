package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.core.TransferData;
import com.sxilverr.lootall.server.LootAllHandler;
import com.sxilverr.lootall.server.TransferService;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
//? if >=1.21.1 {
/*import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
*///?} else {
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
//?}

public final class LootAllNetwork {

    public static final ResourceLocation LOOT_ALL = rl("loot_all");
    public static final ResourceLocation SET_BLOCK_TARGET = rl("set_block_target");
    public static final ResourceLocation CLEAR_TARGET = rl("clear_target");
    public static final ResourceLocation SET_ITEM_TARGET = rl("set_item_target");
    public static final ResourceLocation LOOT_FEEDBACK = rl("loot_feedback");

    private LootAllNetwork() {
    }

    private static ResourceLocation rl(String path) {
        //? if >=1.21.1 {
        /*return ResourceLocation.fromNamespaceAndPath(Config.MOD_ID, path);
        *///?} else {
        return new ResourceLocation(Config.MOD_ID, path);
        //?}
    }

    private static void handleLootAll(ServerPlayer player) {
        if (player != null) {
            LootAllHandler.lootAll(player);
        }
    }

    private static void handleSetBlock(ServerPlayer player, BlockPos pos) {
        if (player == null) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        if (!TransferService.canTargetBlock((ServerLevel) player.level(), pos)) {
            player.displayClientMessage(Component.translatable("message.lootall.target_invalid"), true);
            return;
        }
        TransferData.get(server).setBlockTarget(player.getUUID(), player.level().dimension(), pos);
        Component name = player.level().getBlockState(pos).getBlock().getName();
        player.displayClientMessage(Component.translatable(
                "message.lootall.target_set", name, pos.getX(), pos.getY(), pos.getZ()), true);
    }

    private static void handleClear(ServerPlayer player) {
        if (player == null) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        TransferData.get(server).clear(player.getUUID());
        player.displayClientMessage(Component.translatable("message.lootall.target_cleared"), true);
    }

    private static void handleSetItem(ServerPlayer player, ResourceLocation itemId) {
        if (player == null) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (!TransferService.canTargetItem(player, item)) {
            player.displayClientMessage(Component.translatable("message.lootall.item_target_invalid"), true);
            return;
        }
        TransferData.get(server).setItemTarget(player.getUUID(), itemId);
        Component name = new ItemStack(item).getHoverName();
        player.displayClientMessage(Component.translatable("message.lootall.target_set_item", name), true);
    }

    //? if >=1.21.1 {
    /*public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(LootAllPayload.TYPE, LootAllPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetBlockPayload.TYPE, SetBlockPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearPayload.TYPE, ClearPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetItemPayload.TYPE, SetItemPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LootFeedbackPayload.TYPE, LootFeedbackPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LootAllPayload.TYPE, (payload, context) ->
                handleLootAll(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SetBlockPayload.TYPE, (payload, context) ->
                handleSetBlock(context.player(), payload.pos()));
        ServerPlayNetworking.registerGlobalReceiver(ClearPayload.TYPE, (payload, context) ->
                handleClear(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SetItemPayload.TYPE, (payload, context) ->
                handleSetItem(context.player(), payload.item()));
    }

    public static void sendFeedback(ServerPlayer player, Component message, Optional<Component> transfer) {
        ServerPlayNetworking.send(player, new LootFeedbackPayload(message, transfer));
    }

    public record LootAllPayload() implements CustomPacketPayload {
        public static final Type<LootAllPayload> TYPE = new Type<>(LOOT_ALL);
        public static final StreamCodec<RegistryFriendlyByteBuf, LootAllPayload> CODEC =
                StreamCodec.unit(new LootAllPayload());

        @Override
        public Type<LootAllPayload> type() {
            return TYPE;
        }
    }

    public record SetBlockPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<SetBlockPayload> TYPE = new Type<>(SET_BLOCK_TARGET);
        public static final StreamCodec<RegistryFriendlyByteBuf, SetBlockPayload> CODEC =
                StreamCodec.composite(BlockPos.STREAM_CODEC, SetBlockPayload::pos, SetBlockPayload::new);

        @Override
        public Type<SetBlockPayload> type() {
            return TYPE;
        }
    }

    public record ClearPayload() implements CustomPacketPayload {
        public static final Type<ClearPayload> TYPE = new Type<>(CLEAR_TARGET);
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearPayload> CODEC =
                StreamCodec.unit(new ClearPayload());

        @Override
        public Type<ClearPayload> type() {
            return TYPE;
        }
    }

    public record SetItemPayload(ResourceLocation item) implements CustomPacketPayload {
        public static final Type<SetItemPayload> TYPE = new Type<>(SET_ITEM_TARGET);
        public static final StreamCodec<RegistryFriendlyByteBuf, SetItemPayload> CODEC =
                StreamCodec.composite(ResourceLocation.STREAM_CODEC, SetItemPayload::item, SetItemPayload::new);

        @Override
        public Type<SetItemPayload> type() {
            return TYPE;
        }
    }

    public record LootFeedbackPayload(Component message, Optional<Component> transfer) implements CustomPacketPayload {
        public static final Type<LootFeedbackPayload> TYPE = new Type<>(LOOT_FEEDBACK);
        public static final StreamCodec<RegistryFriendlyByteBuf, LootFeedbackPayload> CODEC = StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC, LootFeedbackPayload::message,
                ComponentSerialization.OPTIONAL_STREAM_CODEC, LootFeedbackPayload::transfer,
                LootFeedbackPayload::new);

        @Override
        public Type<LootFeedbackPayload> type() {
            return TYPE;
        }
    }
    *///?} else {
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(LOOT_ALL, (server, player, handler, buf, responseSender) ->
                server.execute(() -> handleLootAll(player)));
        ServerPlayNetworking.registerGlobalReceiver(SET_BLOCK_TARGET, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            server.execute(() -> handleSetBlock(player, pos));
        });
        ServerPlayNetworking.registerGlobalReceiver(CLEAR_TARGET, (server, player, handler, buf, responseSender) ->
                server.execute(() -> handleClear(player)));
        ServerPlayNetworking.registerGlobalReceiver(SET_ITEM_TARGET, (server, player, handler, buf, responseSender) -> {
            ResourceLocation item = buf.readResourceLocation();
            server.execute(() -> handleSetItem(player, item));
        });
    }

    public static void sendFeedback(ServerPlayer player, Component message, Optional<Component> transfer) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeComponent(message);
        buf.writeBoolean(transfer.isPresent());
        transfer.ifPresent(buf::writeComponent);
        ServerPlayNetworking.send(player, LOOT_FEEDBACK, buf);
    }
    //?}
}
