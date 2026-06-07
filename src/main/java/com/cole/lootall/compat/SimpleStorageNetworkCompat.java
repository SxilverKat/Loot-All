package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;

public final class SimpleStorageNetworkCompat {
    private static boolean failed;
    private static boolean init;
    private static Class<?> cINetworkMaster;
    private static Class<?> cIConnectable;
    private static Method mInsertStack;
    private static Method mGetMasterPos;
    private static Method mDimGetWorld;
    private static Method mDimGetBlockPos;

    private SimpleStorageNetworkCompat() {
    }

    private static boolean ensureInit() {
        if (failed) {
            return false;
        }
        if (init) {
            return true;
        }
        try {
            cINetworkMaster = Class.forName("mrriegel.storagenetwork.api.network.INetworkMaster");
            mInsertStack = cINetworkMaster.getMethod("insertStack", ItemStack.class, boolean.class);
            cIConnectable = Class.forName("mrriegel.storagenetwork.api.capability.IConnectable");
            mGetMasterPos = cIConnectable.getMethod("getMasterPos");
            Class<?> cDimPos = Class.forName("mrriegel.storagenetwork.api.data.DimPos");
            mDimGetWorld = cDimPos.getMethod("getWorld");
            mDimGetBlockPos = cDimPos.getMethod("getBlockPos");
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    public static TransferService.LootSink blockSink(World level, BlockPos pos) {
        if (!ensureInit()) {
            return null;
        }
        try {
            final Object master = resolveMaster(level, pos);
            if (master == null) {
                return null;
            }
            return new TransferService.LootSink() {
                @Override
                public ItemStack insert(ItemStack stack) {
                    try {
                        int count = stack.getCount();
                        int leftover = (Integer) mInsertStack.invoke(master, stack.copy(), false);
                        if (leftover >= count) {
                            return stack;
                        }
                        if (leftover <= 0) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack remainder = stack.copy();
                        remainder.setCount(leftover);
                        return remainder;
                    } catch (Throwable t) {
                        return stack;
                    }
                }
            };
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object resolveMaster(World level, BlockPos pos) throws Exception {
        TileEntity te = level.getTileEntity(pos);
        if (te == null) {
            return null;
        }
        if (cINetworkMaster.isInstance(te)) {
            return te;
        }
        if (cIConnectable.isInstance(te)) {
            Object dimPos = mGetMasterPos.invoke(te);
            if (dimPos == null) {
                return null;
            }
            World masterWorld = (World) mDimGetWorld.invoke(dimPos);
            BlockPos masterPos = (BlockPos) mDimGetBlockPos.invoke(dimPos);
            if (masterWorld == null || masterPos == null) {
                return null;
            }
            TileEntity masterTe = masterWorld.getTileEntity(masterPos);
            if (masterTe != null && cINetworkMaster.isInstance(masterTe)) {
                return masterTe;
            }
        }
        return null;
    }
}
