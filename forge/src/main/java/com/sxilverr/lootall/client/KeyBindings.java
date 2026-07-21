package com.sxilverr.lootall.client;

import com.sxilverr.lootall.Config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
//? if >=1.19 {
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
//?} else if >=1.17 {
/*import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;*/
//?} else {
/*import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;*/
//?}
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Config.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static final KeyMapping LOOT_ALL = new KeyMapping(
            "key.lootall.loot_all",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_MINUS,
            "key.categories.lootall");

    public static final KeyMapping SET_TRANSFER_TARGET = new KeyMapping(
            "key.lootall.set_transfer_target",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_EQUAL,
            "key.categories.lootall");

    //? if >=1.19 {
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(LOOT_ALL);
        if (Config.transferEnabledForRegistration()) {
            event.register(SET_TRANSFER_TARGET);
        }
    }
    //?} else {
    /*@SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.registerKeyBinding(LOOT_ALL);
            if (Config.transferEnabledForRegistration()) {
                ClientRegistry.registerKeyBinding(SET_TRANSFER_TARGET);
            }
        });
    }
    *///?}
}
