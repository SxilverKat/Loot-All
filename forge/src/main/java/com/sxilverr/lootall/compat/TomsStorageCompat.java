package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.server.TransferService;
//? if >=1.19 {
import com.tom.storagemod.tile.StorageTerminalBlockEntity;
//?} else {
/*import com.tom.storagemod.tile.TileEntityStorageTerminal;*/
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TomsStorageCompat {

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        //? if >=1.19 {
        if (!(be instanceof StorageTerminalBlockEntity)) {
            return null;
        }
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) be;
        //?} else {
        /*if (!(be instanceof TileEntityStorageTerminal)) {
            return null;
        }
        TileEntityStorageTerminal terminal = (TileEntityStorageTerminal) be;
        *///?}
        return stack -> {
            try {
                ItemStack remainder = terminal.pushStack(stack.copy());
                return remainder == null ? ItemStack.EMPTY : remainder;
            } catch (Exception e) {
                return stack;
            }
        };
    }
}
