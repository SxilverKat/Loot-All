package com.cole.lootall.compat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Method;

public final class BaublesCompat {
    private static boolean failed;
    private static Method getBaublesHandler;

    private BaublesCompat() {
    }

    public static IItemHandler findItemHandler(EntityPlayer player, Item item) {
        if (failed) {
            return null;
        }
        try {
            if (getBaublesHandler == null) {
                Class<?> api = Class.forName("baubles.api.BaublesApi");
                getBaublesHandler = api.getMethod("getBaublesHandler", EntityPlayer.class);
            }
            Object handlerObj = getBaublesHandler.invoke(null, player);
            if (!(handlerObj instanceof IItemHandler)) {
                return null;
            }
            IItemHandler baubles = (IItemHandler) handlerObj;
            for (int i = 0; i < baubles.getSlots(); i++) {
                ItemStack stack = baubles.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() == item) {
                    IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    if (handler != null) {
                        return handler;
                    }
                }
            }
            return null;
        } catch (Throwable t) {
            failed = true;
            return null;
        }
    }
}
