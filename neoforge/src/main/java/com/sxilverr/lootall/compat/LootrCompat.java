package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.server.LootAllHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import noobanidus.mods.lootr.common.api.data.entity.ILootrCart;
import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;

public class LootrCompat {
    public static boolean isLootrContainer(BlockEntity be) {
        return be instanceof ILootrBlockEntity;
    }

    public static boolean isLootrCart(AbstractMinecartContainer cart) {
        return cart instanceof ILootrCart;
    }

    public static int lootContainer(ServerPlayer player, BlockEntity be) {
        if (!(be instanceof ILootrBlockEntity provider)) {
            return -1;
        }
        return loot(player, provider);
    }

    public static int lootCart(ServerPlayer player, AbstractMinecartContainer cart) {
        if (!(cart instanceof ILootrCart provider)) {
            return -1;
        }
        return loot(player, provider);
    }

    private static int loot(ServerPlayer player, ILootrInfoProvider provider) {
        ILootrInventory inventory = LootrAPI.getInventory(provider, player);
        if (inventory == null) {
            return -1;
        }
        int looted = LootAllHandler.drain(player, inventory);
        inventory.setChanged();
        provider.performOpen(player);
        return looted;
    }
}
