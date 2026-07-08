package com.sxilverr.lootall.client;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.config.LootConfig;

import com.sxilverr.lootall.network.ClearTargetPacket;
import com.sxilverr.lootall.network.LootAllPacket;
import com.sxilverr.lootall.network.SetBlockTargetPacket;
import com.sxilverr.lootall.network.SetItemTargetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Config.MOD_ID, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        TransferFeedback.tick();
        while (KeyBindings.LOOT_ALL.consumeClick()) {
            PacketDistributor.sendToServer(new LootAllPacket());
        }
        if (LootConfig.enableLootingTransfer) {
            while (KeyBindings.SET_TRANSFER_TARGET.consumeClick()) {
                HitResult hit = mc.hitResult;
                if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                    PacketDistributor.sendToServer(new SetBlockTargetPacket(blockHit.getBlockPos()));
                } else {
                    PacketDistributor.sendToServer(new ClearTargetPacket());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!LootConfig.enableLootingTransfer) {
            return;
        }
        if (!KeyBindings.SET_TRANSFER_TARGET.matches(event.getKeyCode(), event.getScanCode())) {
            return;
        }
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || !slot.hasItem()) {
            return;
        }
        ItemStack stack = slot.getItem();
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        PacketDistributor.sendToServer(new SetItemTargetPacket(item));
    }
}
