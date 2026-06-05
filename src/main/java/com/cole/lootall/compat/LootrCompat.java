package com.cole.lootall.compat;

import com.cole.lootall.network.LootAllNetwork;
import com.cole.lootall.network.RefreshBlockPacket;
import com.cole.lootall.server.LootAllHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.network.PacketDistributor;
import noobanidus.mods.lootr.api.blockentity.ILootBlockEntity;
import noobanidus.mods.lootr.api.inventory.ILootrInventory;
import noobanidus.mods.lootr.data.DataStorage;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;

public class LootrCompat {
    public static boolean isLootrContainer(BlockEntity be) {
        return be instanceof ILootBlockEntity;
    }

    public static boolean isLootrCart(AbstractMinecartContainer cart) {
        return cart instanceof LootrChestMinecartEntity;
    }

    public static int lootContainer(ServerPlayer player, BlockEntity be) {
        if (!(be instanceof ILootBlockEntity tile) || !(be instanceof RandomizableContainerBlockEntity rc)) {
            return -1;
        }
        Level level = be.getLevel();
        if (level == null) {
            return -1;
        }
        ILootrInventory inventory = DataStorage.getInventory(
                level, tile.getTileId(), be.getBlockPos(), player, rc, tile::unpackLootTable);
        if (inventory == null) {
            return -1;
        }
        int looted = LootAllHandler.drain(player, inventory);
        inventory.setChanged();
        if (tile.getOpeners().add(player.getUUID())) {
            be.setChanged();
            tile.updatePacketViaState();
            Packet<?> updatePacket = be.getUpdatePacket();
            if (updatePacket != null) {
                player.connection.send(updatePacket);
            }
            LootAllNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new RefreshBlockPacket(be.getBlockPos()));
        }
        return looted;
    }

    public static int lootCart(ServerPlayer player, AbstractMinecartContainer cart) {
        if (!(cart instanceof LootrChestMinecartEntity lootrCart)) {
            return -1;
        }
        ILootrInventory inventory = DataStorage.getInventory(
                cart.level(), lootrCart, player, lootrCart::addLoot);
        if (inventory == null) {
            return -1;
        }
        int looted = LootAllHandler.drain(player, inventory);
        inventory.setChanged();
        if (lootrCart.getOpeners().add(player.getUUID())) {
            lootrCart.setChanged();
        }
        return looted;
    }
}
