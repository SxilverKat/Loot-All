package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.server.TransferService;
import de.ellpeck.prettypipes.network.PipeNetwork;
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.terminal.ItemTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

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
