package com.cole.lootall.client;

import com.cole.lootall.Config;
import com.cole.lootall.LootAll;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = LootAll.MODID, value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(LOOT_ALL);
        if (Config.transferEnabledForRegistration()) {
            event.register(SET_TRANSFER_TARGET);
        }
    }
}
