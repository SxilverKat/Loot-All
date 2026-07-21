package com.sxilverr.lootall.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.sxilverr.lootall.Compat;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class CuriosCompat {
    private static boolean failed;
    private static Method getCuriosInventory;
    private static Method getCurios;
    private static Method getStacks;

    public static IItemHandler findItemHandler(Player player, Item item) {
        if (failed) {
            return null;
        }
        try {
            if (getCuriosInventory == null) {
                Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                getCuriosInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
            }
            Optional<?> inventory = toOptional(getCuriosInventory.invoke(null, player));
            if (!inventory.isPresent()) {
                return null;
            }
            Object curiosInventory = inventory.get();
            if (getCurios == null) {
                getCurios = curiosInventory.getClass().getMethod("getCurios");
            }
            Object curios = getCurios.invoke(curiosInventory);
            if (!(curios instanceof Map)) {
                return null;
            }
            Map<?, ?> stacksByType = (Map<?, ?>) curios;
            for (Object stacksHandler : stacksByType.values()) {
                if (getStacks == null) {
                    getStacks = stacksHandler.getClass().getMethod("getStacks");
                }
                Object stacksObject = getStacks.invoke(stacksHandler);
                if (!(stacksObject instanceof IItemHandler)) {
                    continue;
                }
                IItemHandler stacks = (IItemHandler) stacksObject;
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() == item) {
                        IItemHandler handler = stack.getCapability(Compat.itemHandlerCap()).resolve().orElse(null);
                        if (handler != null) {
                            return handler;
                        }
                    }
                }
            }
            return null;
        } catch (Throwable t) {
            failed = true;
            return null;
        }
    }

    private static Optional<?> toOptional(Object value) {
        if (value instanceof Optional) {
            return (Optional<?>) value;
        }
        try {
            Object resolved = value.getClass().getMethod("resolve").invoke(value);
            if (resolved instanceof Optional) {
                return (Optional<?>) resolved;
            }
        } catch (Throwable ignored) {
        }
        return Optional.empty();
    }
}
