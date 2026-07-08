package com.sxilverr.lootall.server;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.compat.GameStagesCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

public class StageGate {
    private static final boolean GS = ModList.get().isLoaded("gamestages");

    public static boolean canLootAll(ServerPlayer player) {
        return meets(player, Config.lootAllRequiredStage);
    }

    public static boolean canAutoLoot(ServerPlayer player) {
        return meets(player, Config.autoLootRequiredStage);
    }

    public static boolean canTransfer(ServerPlayer player) {
        return meets(player, Config.transferRequiredStage);
    }

    private static boolean meets(ServerPlayer player, String stage) {
        if (stage == null || stage.isBlank()) {
            return true;
        }
        if (!GS) {
            return true;
        }
        return GameStagesCompat.hasStage(player, stage);
    }
}
