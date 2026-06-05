package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.terminal.ItemTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Pretty Pipes support — the Item Terminal (and Crafting Terminal).
 *
 * <p>Pretty Pipes stores items in regular inventories joined to a {@code PipeNetwork} by pipes
 * (those chests already work via the generic path). The Item Terminal is the unified
 * "dump into the whole network" point, but it does NOT expose a vanilla {@code IItemHandler}
 * (its {@code getCapability} only answers the mod's pipe-connectable cap), so it needs compat.
 *
 * <p>We route each looted stack straight into the network via
 * {@code PipeNetwork.routeItem(startPipe, avoidDest, stack, preventOversending)} — exactly what the
 * terminal's tick does. This is essential because loot-all runs in a SINGLE tick: the terminal's
 * own "send to network" input buffer is only 6 slots and is drained by its tick (which doesn't run
 * mid-loot), so feeding the buffer caps a haul at ~6 stacks. routeItem instead spawns traveling pipe
 * items toward available destinations immediately and returns the remainder (bytecode-verified:
 * arg2 is the destination to avoid = the terminal, the final boolean is preventOversending NOT
 * simulate, and it executes via routeItemToLocation). {@code CraftingTerminalBlockEntity extends
 * ItemTerminalBlockEntity}, so one instanceof covers both.
 */
public class PrettyPipesCompat {

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ItemTerminalBlockEntity terminal)) {
            return null;
        }
        PipeBlockEntity pipe = terminal.getConnectedPipe();
        if (pipe == null) {
            return null;
        }
        PipeNetwork network = PipeNetwork.get(level);
        if (network == null) {
            return null;
        }
        BlockPos pipePos = pipe.getBlockPos();
        BlockPos terminalPos = terminal.getBlockPos();
        return stack -> {
            try {
                ItemStack remainder = network.routeItem(pipePos, terminalPos, stack.copy(), true);
                return remainder == null ? ItemStack.EMPTY : remainder;
            } catch (Exception e) {
                return stack;
            }
        };
    }
}
