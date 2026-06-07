package com.cole.lootall.client;

import com.cole.lootall.Config;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public final class KeyBindings {
    public static final KeyBinding LOOT_ALL = new KeyBinding(
            "key.lootall.loot_all", KeyConflictContext.IN_GAME, Keyboard.KEY_MINUS, "key.categories.lootall");

    public static final KeyBinding SET_TRANSFER_TARGET = new KeyBinding(
            "key.lootall.set_transfer_target", KeyConflictContext.IN_GAME, Keyboard.KEY_EQUALS, "key.categories.lootall");

    private KeyBindings() {
    }

    public static void register() {
        ClientRegistry.registerKeyBinding(LOOT_ALL);
        if (Config.transferEnabledForRegistration()) {
            ClientRegistry.registerKeyBinding(SET_TRANSFER_TARGET);
        }
    }
}
