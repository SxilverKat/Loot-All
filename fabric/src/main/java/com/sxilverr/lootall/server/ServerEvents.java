package com.sxilverr.lootall.server;

import com.sxilverr.lootall.config.LootConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

public final class ServerEvents {
    private static int ticks;

    private ServerEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!LootConfig.autoLooting) {
                return;
            }
            if (++ticks < LootConfig.autoLootingTimer * 20) {
                return;
            }
            ticks = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.isSpectator()) {
                    LootAllHandler.lootAll(player, true);
                }
            }
        });
    }
}
