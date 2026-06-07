package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class ForestryBackpackCompat {
    private static boolean failed;
    private static boolean init;
    private static Class<?> cItemBackpack;
    private static Method mGetBackpackSize;
    private static Constructor<?> ctorInventory;

    private ForestryBackpackCompat() {
    }

    private static boolean ensureInit() {
        if (failed) {
            return false;
        }
        if (init) {
            return true;
        }
        try {
            cItemBackpack = Class.forName("forestry.storage.items.ItemBackpack");
            mGetBackpackSize = cItemBackpack.getMethod("getBackpackSize");
            Class<?> cInv = Class.forName("forestry.storage.inventory.ItemInventoryBackpack");
            ctorInventory = cInv.getConstructor(EntityPlayer.class, int.class, ItemStack.class);
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    public static boolean isBackpack(ItemStack stack) {
        if (!ensureInit() || stack.isEmpty()) {
            return false;
        }
        return cItemBackpack.isInstance(stack.getItem());
    }

    public static TransferService.ResolvedSink itemSink(EntityPlayer player, ItemStack stack) {
        if (!isBackpack(stack)) {
            return null;
        }
        try {
            int size = (Integer) mGetBackpackSize.invoke(stack.getItem());
            Object inventory = ctorInventory.newInstance(player, size, stack);
            if (!(inventory instanceof IInventory)) {
                return null;
            }
            final IItemHandler handler = new InvWrapper((IInventory) inventory);
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
