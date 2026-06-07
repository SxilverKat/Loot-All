package com.cole.lootall.compat;

import net.minecraft.entity.player.EntityPlayer;

import java.lang.reflect.Method;

public final class GameStagesCompat {
    private static boolean failed;
    private static Method hasStage;

    private GameStagesCompat() {
    }

    public static boolean hasStage(EntityPlayer player, String stage) {
        if (failed) {
            return false;
        }
        try {
            if (hasStage == null) {
                Class<?> helper = Class.forName("net.darkhax.gamestages.GameStageHelper");
                hasStage = helper.getMethod("hasStage", EntityPlayer.class, String.class);
            }
            Object result = hasStage.invoke(null, player, stage);
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }
}
