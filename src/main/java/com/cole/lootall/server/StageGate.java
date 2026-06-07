package com.cole.lootall.server;

import com.cole.lootall.Config;
import com.cole.lootall.compat.GameStagesCompat;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Loader;

public final class StageGate {
    private static final boolean GS = Loader.isModLoaded("gamestages");

    private StageGate() {
    }

    public static boolean canLootAll(EntityPlayerMP player) {
        return meets(player, Config.lootAllRequiredStage);
    }

    public static boolean canAutoLoot(EntityPlayerMP player) {
        return meets(player, Config.autoLootRequiredStage);
    }

    public static boolean canTransfer(EntityPlayerMP player) {
        return meets(player, Config.transferRequiredStage);
    }

    private static boolean meets(EntityPlayerMP player, String stage) {
        if (stage == null || stage.trim().isEmpty()) {
            return true;
        }
        if (!GS) {
            return true;
        }
        return GameStagesCompat.hasStage(player, stage);
    }
}
