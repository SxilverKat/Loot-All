package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.server.TransferService;
import com.tom.storagemod.block.entity.StorageTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TomsStorageCompat {

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof StorageTerminalBlockEntity terminal)) {
            return null;
        }
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
