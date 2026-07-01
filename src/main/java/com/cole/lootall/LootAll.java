package com.cole.lootall;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(LootAll.MODID)
public class LootAll {
    public static final String MODID = "lootall";

    public LootAll(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
