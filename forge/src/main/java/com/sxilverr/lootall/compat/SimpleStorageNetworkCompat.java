package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.server.TransferService;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SimpleStorageNetworkCompat {

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        TileMain master = resolveMaster(level, pos);
        if (master == null) {
            return null;
        }
        return stack -> {
            try {
                int count = stack.getCount();
                int remaining = master.insertStack(stack.copy(), false);
                if (remaining >= count) {
                    return stack;
                }
                if (remaining <= 0) {
                    return ItemStack.EMPTY;
                }
                ItemStack rem = stack.copy();
                rem.setCount(remaining);
                return rem;
            } catch (Exception e) {
                return stack;
            }
        };
    }

    private static TileMain resolveMaster(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileMain) {
            return (TileMain) be;
        }
        if (be instanceof TileConnectable) {
            TileConnectable connectable = (TileConnectable) be;
            DimPos mainPos = connectable.getMain();
            if (mainPos != null) {
                try {
                    return mainPos.getTileEntity(TileMain.class, level);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}
