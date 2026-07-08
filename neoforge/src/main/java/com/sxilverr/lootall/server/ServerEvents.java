package com.sxilverr.lootall.server;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.config.LootConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Config.MOD_ID)
public class ServerEvents {
    private static int ticks;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!LootConfig.autoLooting) {
            return;
        }
        if (++ticks < LootConfig.autoLootingTimer * 20) {
            return;
        }
        ticks = 0;

        MinecraftServer server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isSpectator()) {
                LootAllHandler.lootAll(player, true);
            }
        }
    }
}
