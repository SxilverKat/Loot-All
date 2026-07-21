package com.sxilverr.lootall.forge;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.network.LootAllNetwork;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Config.MOD_ID)
public final class LootAllForge {

    public LootAllForge() {
        LootAllNetwork.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
