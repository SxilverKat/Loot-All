package com.sxilverr.lootall.client;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.config.LootConfig;

import com.sxilverr.lootall.network.ClearTargetPacket;
import com.sxilverr.lootall.network.LootAllNetwork;
import com.sxilverr.lootall.network.LootAllPacket;
import com.sxilverr.lootall.network.SetBlockTargetPacket;
import com.sxilverr.lootall.network.SetItemTargetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
//? if >=1.17 {
import net.minecraftforge.client.event.ScreenEvent;
//?} else {
/*import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;*/
//?}
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Config.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        TransferFeedback.tick();
        while (KeyBindings.LOOT_ALL.consumeClick()) {
            LootAllNetwork.CHANNEL.sendToServer(new LootAllPacket());
        }
        if (LootConfig.enableLootingTransfer) {
            while (KeyBindings.SET_TRANSFER_TARGET.consumeClick()) {
                HitResult hit = mc.hitResult;
                if (hit instanceof BlockHitResult && hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    LootAllNetwork.CHANNEL.sendToServer(new SetBlockTargetPacket(blockHit.getBlockPos()));
                } else {
                    LootAllNetwork.CHANNEL.sendToServer(new ClearTargetPacket());
                }
            }
        }
    }

    //? if <1.17 {
    /*@SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        TransferFeedback.render(event.getMatrixStack(),
                event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
    }
    *///?}

    //? if >=1.19 {
    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        onKey(event.getKeyCode(), event.getScanCode(), event.getScreen());
    }
    //?} else if >=1.17 {
    /*@SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        onKey(event.getKeyCode(), event.getScanCode(), event.getScreen());
    }
    *///?} else {
    /*@SubscribeEvent
    public static void onScreenKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        onKey(event.getKeyCode(), event.getScanCode(), event.getGui());
    }
    *///?}

    private static void onKey(int keyCode, int scanCode, Screen screen) {
        if (!LootConfig.enableLootingTransfer) {
            return;
        }
        if (!KeyBindings.SET_TRANSFER_TARGET.matches(keyCode, scanCode)) {
            return;
        }
        if (!(screen instanceof AbstractContainerScreen)) {
            return;
        }
        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
        Slot slot = containerScreen.getSlotUnderMouse();
        if (slot == null || !slot.hasItem()) {
            return;
        }
        ItemStack stack = slot.getItem();
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stack.getItem());
        LootAllNetwork.CHANNEL.sendToServer(new SetItemTargetPacket(item));
    }
}
