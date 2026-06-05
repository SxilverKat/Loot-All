package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Simple Storage Network (Lothrazar, mod id "storagenetwork") loot-transfer support.
 *
 * <p>Items live in inventories joined to the network by Storage Link cables; the central
 * {@code TileMain} (Storage Network Master) distributes inserts across them. Access blocks
 * (Request Table, cables, etc.) extend {@code TileConnectable} and expose the master's position
 * via {@link TileConnectable#getMain()}. So binding to ANY network block resolves the master and
 * inserts network-wide:
 * <ul>
 *   <li>target is the Master itself → use it directly;</li>
 *   <li>target is a Request Table / any connectable → {@code getMain()} → resolve the master.</li>
 * </ul>
 *
 * <p>{@code TileMain.insertStack(stack, simulate)} returns the count that could NOT be stored
 * (remainder count, bytecode-verified — it returns the leftover stack's size), so remainder
 * count maps straight to the {@link TransferService.LootSink} contract.
 */
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
        if (be instanceof TileMain master) {
            return master;
        }
        if (be instanceof TileConnectable connectable) {
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
