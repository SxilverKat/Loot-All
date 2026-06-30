package com.cole.lootall.client;

import com.cole.lootall.Config;
import com.cole.lootall.LootAll;
import com.cole.lootall.network.ClearTargetPacket;
import com.cole.lootall.network.LootAllNetwork;
import com.cole.lootall.network.LootAllPacket;
import com.cole.lootall.network.SetBlockTargetPacket;
import com.cole.lootall.network.SetItemTargetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = LootAll.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        while (KeyBindings.LOOT_ALL.consumeClick()) {
            LootAllNetwork.CHANNEL.send(new LootAllPacket(), PacketDistributor.SERVER.noArg());
        }
        if (Config.enableLootingTransfer) {
            while (KeyBindings.SET_TRANSFER_TARGET.consumeClick()) {
                HitResult hit = mc.hitResult;
                if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                    LootAllNetwork.CHANNEL.send(new SetBlockTargetPacket(blockHit.getBlockPos()), PacketDistributor.SERVER.noArg());
                } else {
                    LootAllNetwork.CHANNEL.send(new ClearTargetPacket(), PacketDistributor.SERVER.noArg());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!Config.enableLootingTransfer) {
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
        LootAllNetwork.CHANNEL.send(new SetItemTargetPacket(item), PacketDistributor.SERVER.noArg());
    }
}
