package com.sxilverr.lootall.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sxilverr.lootall.Config;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    public static KeyMapping LOOT_ALL;
    public static KeyMapping SET_TRANSFER_TARGET;

    private KeyBindings() {
    }

    public static void register() {
        LOOT_ALL = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.lootall.loot_all",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_MINUS,
                "key.categories.lootall"));
        if (Config.transferEnabledForRegistration()) {
            SET_TRANSFER_TARGET = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                    "key.lootall.set_transfer_target",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_EQUAL,
                    "key.categories.lootall"));
        }
    }
}
