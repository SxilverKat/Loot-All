package com.sxilverr.lootall.fabric;

import com.sxilverr.lootall.client.ClientEvents;
import com.sxilverr.lootall.client.KeyBindings;
import com.sxilverr.lootall.client.TransferFeedback;
import com.sxilverr.lootall.network.LootAllNetworkClient;
import net.fabricmc.api.ClientModInitializer;

public final class LootAllFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        LootAllNetworkClient.registerClient();
        ClientEvents.register();
        TransferFeedback.register();
    }
}
