package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.server.TransferService;
import de.ellpeck.prettypipes.network.PipeNetwork;
//? if >=1.18 {
import de.ellpeck.prettypipes.pipe.PipeBlockEntity;
import de.ellpeck.prettypipes.terminal.ItemTerminalBlockEntity;
//?} else {
/*import de.ellpeck.prettypipes.pipe.PipeTileEntity;
import de.ellpeck.prettypipes.terminal.ItemTerminalTileEntity;*/
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PrettyPipesCompat {

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        //? if >=1.18 {
        if (!(be instanceof ItemTerminalBlockEntity)) {
            return null;
        }
        ItemTerminalBlockEntity terminal = (ItemTerminalBlockEntity) be;
        PipeBlockEntity pipe = terminal.getConnectedPipe();
        //?} else {
        /*if (!(be instanceof ItemTerminalTileEntity)) {
            return null;
        }
        ItemTerminalTileEntity terminal = (ItemTerminalTileEntity) be;
        PipeTileEntity pipe = terminal.getConnectedPipe();
        *///?}
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
