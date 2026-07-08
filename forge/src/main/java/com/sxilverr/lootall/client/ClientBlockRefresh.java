package com.sxilverr.lootall.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ClientBlockRefresh {
    public static void refresh(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockState state = mc.level.getBlockState(pos);
        mc.levelRenderer.blockChanged(mc.level, pos, state, state, 8);
    }
}
