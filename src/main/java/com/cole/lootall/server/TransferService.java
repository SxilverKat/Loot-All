package com.cole.lootall.server;

import com.cole.lootall.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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
        if (!Config.enableLootingTransfer) {
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
        if (Config.transferRequireSameDimension && !sameDimension) {
            return null;
        }
        BlockPos pos = block.pos();
        if (sameDimension && Config.maxLootTransferDistance > 0) {
            double maxSq = (double) Config.maxLootTransferDistance * Config.maxLootTransferDistance;
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxSq) {
                return null;
            }
        }
        ChunkPos chunkPos = new ChunkPos(pos);
        if (Config.transferRequireLoadedChunk
                && targetLevel.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
            return null;
        }
        BlockEntity be = targetLevel.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        IItemHandler handler = findHandler(be);
        if (handler == null) {
            return null;
        }
        return new ResolvedSink(stack -> ItemHandlerHelper.insertItemStacked(handler, stack, false),
                targetLevel.getBlockState(pos).getBlock().getName());
    }

    private static ResolvedSink resolveItemSink(ServerPlayer player, Item item) {
        IItemHandler handler = resolveItemHandler(player, item);
        if (handler == null) {
            return null;
        }
        return new ResolvedSink(stack -> ItemHandlerHelper.insertItemStacked(handler, stack, false),
                new ItemStack(item).getHoverName());
    }

    public static IItemHandler resolveItemHandler(ServerPlayer player, Item item) {
        return inventoryItemHandler(player, item);
    }

    public static boolean canTargetBlock(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && findHandler(be) != null;
    }

    public static boolean canTargetItem(ServerPlayer player, Item item) {
        return resolveItemHandler(player, item) != null;
    }

    private static IItemHandler inventoryItemHandler(ServerPlayer player, Item item) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

    public static IItemHandler findHandler(BlockEntity be) {
        IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
        if (handler != null) {
            return handler;
        }
        for (Direction direction : Direction.values()) {
            handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).resolve().orElse(null);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
}
