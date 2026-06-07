package com.cole.lootall.server;

import com.cole.lootall.Config;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ServerEvents {
    private int ticks;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Config.autoLooting) {
            return;
        }
        if (++ticks < Config.autoLootingTimer * 20) {
            return;
        }
        ticks = 0;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return;
        }
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (!player.isSpectator() && StageGate.canAutoLoot(player)) {
                LootAllHandler.lootAll(player, true);
            }
        }
    }
}
