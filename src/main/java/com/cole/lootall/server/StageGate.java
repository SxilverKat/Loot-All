package com.cole.lootall.server;

import com.cole.lootall.Config;
import com.cole.lootall.compat.GameStagesCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

/**
 * Game Stages gating for Loot All's abilities. Each ability has a configurable required stage
 * (empty = no requirement). Enforcement only happens when the Game Stages mod is installed; if it
 * is not, configured stages are ignored so a requirement can never permanently lock players out on
 * packs that lack Game Stages. {@link GameStagesCompat} is only reached after the {@code GS} guard,
 * keeping the {@code net.darkhax.gamestages} classes off the load path when the mod is absent.
 */
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
