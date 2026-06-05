package com.cole.lootall.compat;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.server.level.ServerPlayer;

/**
 * Game Stages integration. Isolated so the {@code net.darkhax.gamestages} classes are only
 * class-loaded when the "gamestages" mod is present — callers must guard with the ModList check
 * in {@link com.cole.lootall.server.StageGate} before touching this class.
 */
public class GameStagesCompat {

    public static boolean hasStage(ServerPlayer player, String stage) {
        try {
            return GameStageHelper.hasStage(player, stage);
        } catch (Exception e) {
            return false;
        }
    }
}
