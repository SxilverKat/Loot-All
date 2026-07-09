package com.sxilverr.lootall.client;

import com.sxilverr.lootall.config.LootConfig;
import com.sxilverr.lootall.fabric.mixin.AbstractContainerScreenAccessor;
import com.sxilverr.lootall.network.LootAllNetworkClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ClientEvents {

    private ClientEvents() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            TransferFeedback.tick();
            while (KeyBindings.LOOT_ALL.consumeClick()) {
                LootAllNetworkClient.sendLootAll();
            }
            if (LootConfig.enableLootingTransfer && KeyBindings.SET_TRANSFER_TARGET != null) {
                while (KeyBindings.SET_TRANSFER_TARGET.consumeClick()) {
                    HitResult hit = client.hitResult;
                    if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                        LootAllNetworkClient.sendSetBlockTarget(blockHit.getBlockPos());
                    } else {
                        LootAllNetworkClient.sendClearTarget();
                    }
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?>)) {
                return;
            }
            ScreenKeyboardEvents.afterKeyPress(screen).register((handledScreen, key, scancode, modifiers) -> {
                if (!LootConfig.enableLootingTransfer || KeyBindings.SET_TRANSFER_TARGET == null) {
                    return;
                }
                if (!KeyBindings.SET_TRANSFER_TARGET.matches(key, scancode)) {
                    return;
                }
                if (!(handledScreen instanceof AbstractContainerScreen<?> containerScreen)) {
                    return;
                }
                Slot slot = ((AbstractContainerScreenAccessor) containerScreen).lootall$getHoveredSlot();
                if (slot == null || !slot.hasItem()) {
                    return;
                }
                ItemStack stack = slot.getItem();
                ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
                LootAllNetworkClient.sendSetItemTarget(item);
            });
        });
    }
}
