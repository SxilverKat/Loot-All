package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import com.tom.storagemod.block.entity.StorageTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Tom's Simple Storage loot-transfer support.
 *
 * <p>Tom's storage lives in a network aggregated by the Inventory Connector. The Connector and
 * Inventory Cable Connector both expose the combined network as a standard {@code IItemHandler}
 * capability, so binding to those already works through the generic path — no code needed.
 *
 * <p>The gap is the Storage Terminal / Crafting Terminal (the block players actually look at):
 * it holds the network handler privately and does NOT expose a capability. It does, however,
 * expose a public {@link StorageTerminalBlockEntity#pushStack(ItemStack)} that inserts into the
 * network and returns the remainder — exactly the {@link TransferService.LootSink} contract.
 * {@code CraftingTerminalBlockEntity extends StorageTerminalBlockEntity}, so one instanceof
 * covers both terminals.
 */
public class TomsStorageCompat {

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof StorageTerminalBlockEntity terminal)) {
            return null;
        }
        return stack -> {
            try {
                // pushStack inserts as much as fits and returns the leftover (EMPTY if fully
                // stored). Relies on the terminal having resolved its connector during ticking;
                // if it hasn't (unconnected / unloaded), it throws -> fall back to the stack.
                ItemStack remainder = terminal.pushStack(stack.copy());
                return remainder == null ? ItemStack.EMPTY : remainder;
            } catch (Exception e) {
                return stack;
            }
        };
    }
}
