package com.sxilverr.lootall.fabric;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.network.LootAllNetwork;
import com.sxilverr.lootall.server.ServerEvents;
import net.fabricmc.api.ModInitializer;

public final class LootAllFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Config.load();
        LootAllNetwork.registerServer();
        ServerEvents.register();
    }
}
