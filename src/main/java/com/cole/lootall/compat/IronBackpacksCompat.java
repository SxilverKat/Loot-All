package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class IronBackpacksCompat {
    private static boolean failed;
    private static boolean init;
    private static Capability<?> invCap;
    private static Method mFromStack;
    private static Method mGetInventory;
    private static Method mApplyPackInfo;

    private IronBackpacksCompat() {
    }

    private static boolean ensureInit() {
        if (failed) {
            return false;
        }
        if (init) {
            return true;
        }
        try {
            Class<?> cHelper = Class.forName("gr8pefish.ironbackpacks.api.backpack.inventory.IronBackpacksInventoryHelper");
            Field capField = cHelper.getField("BACKPACK_INV_CAPABILITY");
            invCap = (Capability<?>) capField.get(null);
            Class<?> cInfo = Class.forName("gr8pefish.ironbackpacks.api.backpack.BackpackInfo");
            mFromStack = cInfo.getMethod("fromStack", ItemStack.class);
            mGetInventory = cInfo.getMethod("getInventory");
            Class<?> cApi = Class.forName("gr8pefish.ironbackpacks.api.IronBackpacksAPI");
            mApplyPackInfo = cApi.getMethod("applyPackInfo", ItemStack.class, cInfo);
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean isBackpack(ItemStack stack) {
        if (!ensureInit() || stack.isEmpty()) {
            return false;
        }
        return stack.hasCapability((Capability<Object>) invCap, null);
    }

    public static TransferService.ResolvedSink itemSink(final ItemStack stack) {
        if (!isBackpack(stack)) {
            return null;
        }
        try {
            final Object info = mFromStack.invoke(null, stack);
            if (info == null) {
                return null;
            }
            Object handlerObj = mGetInventory.invoke(info);
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
                    try {
                        mApplyPackInfo.invoke(null, stack, info);
                    } catch (Throwable ignored) {
                    }
                }
            };
            return new TransferService.ResolvedSink(sink, TransferService.itemDisplayName(stack), writeBack);
        } catch (Throwable t) {
            return null;
        }
    }
}
