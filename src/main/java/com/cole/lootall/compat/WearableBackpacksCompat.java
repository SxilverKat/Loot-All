package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class WearableBackpacksCompat {
    private static boolean failed;
    private static boolean init;
    private static Capability<?> backpackCap;
    private static Method mGetData;
    private static Class<?> cDataItems;
    private static Method mGetItems;

    private WearableBackpacksCompat() {
    }

    private static boolean ensureInit() {
        if (failed) {
            return false;
        }
        if (init) {
            return true;
        }
        try {
            Class<?> cBackpack = Class.forName("net.mcft.copy.backpacks.api.IBackpack");
            Field capField = cBackpack.getField("CAPABILITY");
            backpackCap = (Capability<?>) capField.get(null);
            mGetData = cBackpack.getMethod("getData");
            cDataItems = Class.forName("net.mcft.copy.backpacks.misc.BackpackDataItems");
            mGetItems = cDataItems.getMethod("getItems");
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static TransferService.ResolvedSink blockSink(World level, BlockPos pos) {
        if (!ensureInit()) {
            return null;
        }
        try {
            final TileEntity te = level.getTileEntity(pos);
            if (te == null || !te.hasCapability((Capability<Object>) backpackCap, null)) {
                return null;
            }
            Object backpack = te.getCapability((Capability<Object>) backpackCap, null);
            if (backpack == null) {
                return null;
            }
            Object data = mGetData.invoke(backpack);
            if (data == null || !cDataItems.isInstance(data)) {
                return null;
            }
            Object handlerObj = mGetItems.invoke(data);
            if (!(handlerObj instanceof IItemHandler)) {
                return null;
            }
            final IItemHandler handler = (IItemHandler) handlerObj;
            TransferService.LootSink sink = new TransferService.LootSink() {
                @Override
                public ItemStack insert(ItemStack s) {
                    return ItemHandlerHelper.insertItemStacked(handler, s, false);
                }
            };
            Runnable writeBack = new Runnable() {
                @Override
                public void run() {
                    te.markDirty();
                }
            };
            return new TransferService.ResolvedSink(sink, TransferService.blockDisplayName(level, pos), writeBack);
        } catch (Throwable t) {
            return null;
        }
    }
}
