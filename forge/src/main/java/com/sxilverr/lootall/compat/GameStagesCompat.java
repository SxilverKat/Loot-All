package com.sxilverr.lootall.compat;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.server.level.ServerPlayer;

public class GameStagesCompat {

    public static boolean hasStage(ServerPlayer player, String stage) {
        try {
            return GameStageHelper.hasStage(player, stage);
        } catch (Exception e) {
            return false;
        }
    }
}
