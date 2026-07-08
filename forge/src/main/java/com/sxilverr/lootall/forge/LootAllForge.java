package com.sxilverr.lootall.forge;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.network.LootAllNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Config.MOD_ID)
public final class LootAllForge {

    public LootAllForge(FMLJavaModLoadingContext context) {
        LootAllNetwork.register();
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
