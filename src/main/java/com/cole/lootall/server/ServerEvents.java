package com.cole.lootall.server;

import com.cole.lootall.Config;
import com.cole.lootall.LootAll;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = LootAll.MODID)
public class ServerEvents {
    private static int ticks;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!Config.autoLooting) {
            return;
        }
        if (++ticks < Config.autoLootingTimer * 20) {
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
