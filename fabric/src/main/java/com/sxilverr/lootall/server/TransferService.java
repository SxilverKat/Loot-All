package com.sxilverr.lootall.server;

import com.sxilverr.lootall.config.LootConfig;
import com.sxilverr.lootall.core.TransferData;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

public class TransferService {

    public interface LootSink {
        ItemStack insert(ItemStack stack);
    }

    public record ResolvedSink(LootSink sink, Component name, Runnable onComplete) {
        public ResolvedSink(LootSink sink, Component name) {
            this(sink, name, null);
        }
    }

    public static ResolvedSink resolveSink(ServerPlayer player) {
        if (!LootConfig.enableLootingTransfer) {
            return null;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }
        TransferData.Target target = TransferData.get(server).getTarget(player.getUUID());
        if (target instanceof TransferData.ItemTarget itemTarget) {
            return resolveItemSink(player, BuiltInRegistries.ITEM.get(itemTarget.item()));
        }
        if (!(target instanceof TransferData.BlockTarget block)) {
            return null;
        }
        ServerLevel targetLevel = server.getLevel(block.dimension());
        if (targetLevel == null) {
            return null;
        }
        boolean sameDimension = targetLevel.dimension() == player.level().dimension();
        if (LootConfig.transferRequireSameDimension && !sameDimension) {
            return null;
        }
        BlockPos pos = block.pos();
        if (sameDimension && LootConfig.maxLootTransferDistance > 0) {
            double maxSq = (double) LootConfig.maxLootTransferDistance * LootConfig.maxLootTransferDistance;
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxSq) {
                return null;
            }
        }
        ChunkPos chunkPos = new ChunkPos(pos);
        if (LootConfig.transferRequireLoadedChunk
                && targetLevel.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
            return null;
        }
        Storage<ItemVariant> storage = blockStorage(targetLevel, pos);
        if (storage == null || !storage.supportsInsertion()) {
            return null;
        }
        return new ResolvedSink(stack -> insert(storage, stack),
                targetLevel.getBlockState(pos).getBlock().getName());
    }

    private static ResolvedSink resolveItemSink(ServerPlayer player, Item item) {
        return null;
    }

    public static boolean canTargetBlock(ServerLevel level, BlockPos pos) {
        Storage<ItemVariant> storage = blockStorage(level, pos);
        return storage != null && storage.supportsInsertion();
    }

    public static boolean canTargetItem(ServerPlayer player, Item item) {
        return false;
    }

    private static Storage<ItemVariant> blockStorage(ServerLevel level, BlockPos pos) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, null);
        if (storage != null) {
            return storage;
        }
        for (Direction direction : Direction.values()) {
            storage = ItemStorage.SIDED.find(level, pos, direction);
            if (storage != null) {
                return storage;
            }
        }
        return null;
    }

    static ItemStack insert(Storage<ItemVariant> storage, ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        ItemVariant variant = ItemVariant.of(stack);
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(variant, stack.getCount(), transaction);
            transaction.commit();
            if (inserted <= 0) {
                return stack;
            }
            if (inserted >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - (int) inserted);
            return remainder;
        }
    }
}
