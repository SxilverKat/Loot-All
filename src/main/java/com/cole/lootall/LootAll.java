package com.cole.lootall;

import com.cole.lootall.network.LootAllNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LootAll.MODID)
public class LootAll {
    public static final String MODID = "lootall";

    public LootAll(FMLJavaModLoadingContext context) {
        LootAllNetwork.register();
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
