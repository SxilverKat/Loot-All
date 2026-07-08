package com.sxilverr.lootall.server;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.config.LootConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = Config.MOD_ID)
public class ServerEvents {
    private static int ticks;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !LootConfig.autoLooting) {
            return;
        }
        if (++ticks < LootConfig.autoLootingTimer * 20) {
            return;
        }
        ticks = 0;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isSpectator() && StageGate.canAutoLoot(player)) {
                LootAllHandler.lootAll(player, true);
            }
        }
    }
}
