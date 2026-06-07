package com.cole.lootall.client;

import com.cole.lootall.Config;
import com.cole.lootall.LootAll;
import com.cole.lootall.network.ClearTargetMessage;
import com.cole.lootall.network.LootAllMessage;
import com.cole.lootall.network.PacketHandler;
import com.cole.lootall.network.SetBlockTargetMessage;
import com.cole.lootall.network.SetItemTargetMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.input.Keyboard;

public class ClientEvents {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            return;
        }
        TransferFeedback.tick();
        while (KeyBindings.LOOT_ALL.isPressed()) {
            PacketHandler.INSTANCE.sendToServer(new LootAllMessage());
        }
        if (Config.enableLootingTransfer) {
            while (KeyBindings.SET_TRANSFER_TARGET.isPressed()) {
                RayTraceResult hit = mc.objectMouseOver;
                if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                    PacketHandler.INSTANCE.sendToServer(new SetBlockTargetMessage(hit.getBlockPos()));
                } else {
                    PacketHandler.INSTANCE.sendToServer(new ClearTargetMessage());
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!Config.enableLootingTransfer) {
            return;
        }
        if (!Keyboard.getEventKeyState()) {
            return;
        }
        if (Keyboard.getEventKey() != KeyBindings.SET_TRANSFER_TARGET.getKeyCode()) {
            return;
        }
        if (!(event.getGui() instanceof GuiContainer)) {
            return;
        }
        Slot slot = ((GuiContainer) event.getGui()).getSlotUnderMouse();
        if (slot == null || !slot.getHasStack()) {
            return;
        }
        ItemStack stack = slot.getStack();
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (item != null) {
            PacketHandler.INSTANCE.sendToServer(new SetItemTargetMessage(item));
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (LootAll.MODID.equals(event.getModID())) {
            Config.load();
        }
    }
}
