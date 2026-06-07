package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.lang.reflect.Method;

public final class EnderPouchCompat {
    private static boolean failed;
    private static boolean init;
    private static Class<?> cPouch;
    private static Method mReadFromStack;
    private static Method mInstance;
    private static Method mGetStorage;

    private EnderPouchCompat() {
    }

    private static boolean ensureInit() {
        if (failed) {
            return false;
        }
        if (init) {
            return true;
        }
        try {
            cPouch = Class.forName("codechicken.enderstorage.item.ItemEnderPouch");
            Class<?> cFreq = Class.forName("codechicken.enderstorage.api.Frequency");
            mReadFromStack = cFreq.getMethod("readFromStack", ItemStack.class);
            Class<?> cManager = Class.forName("codechicken.enderstorage.manager.EnderStorageManager");
            mInstance = cManager.getMethod("instance", boolean.class);
            mGetStorage = cManager.getMethod("getStorage", cFreq, String.class);
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    public static boolean isEnderPouch(ItemStack stack) {
        if (!ensureInit() || stack.isEmpty()) {
            return false;
        }
        return cPouch.isInstance(stack.getItem());
    }

    public static TransferService.ResolvedSink itemSink(EntityPlayer player, ItemStack stack) {
        if (!isEnderPouch(stack)) {
            return null;
        }
        try {
            Object freq = mReadFromStack.invoke(null, stack);
            if (freq == null) {
                return null;
            }
            Object manager = mInstance.invoke(null, Boolean.FALSE);
            if (manager == null) {
                return null;
            }
            Object storage = mGetStorage.invoke(manager, freq, "item");
            if (!(storage instanceof IInventory)) {
                return null;
            }
            final IItemHandler handler = new InvWrapper((IInventory) storage);
            TransferService.LootSink sink = new TransferService.LootSink() {
                @Override
                public ItemStack insert(ItemStack s) {
                    return ItemHandlerHelper.insertItemStacked(handler, s, false);
                }
            };
            return new TransferService.ResolvedSink(sink, TransferService.itemDisplayName(stack));
        } catch (Throwable t) {
            return null;
        }
    }
}
