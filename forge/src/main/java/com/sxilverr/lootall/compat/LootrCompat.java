package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.Compat;
import com.sxilverr.lootall.network.LootAllNetwork;
import com.sxilverr.lootall.network.RefreshBlockPacket;
import com.sxilverr.lootall.server.LootAllHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
//? if >=1.17 {
import net.minecraftforge.network.PacketDistributor;
//?} else {
/*import net.minecraftforge.fml.network.PacketDistributor;*/
//?}
import noobanidus.mods.lootr.data.DataStorage;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
//? if >=1.18 {
import noobanidus.mods.lootr.api.blockentity.ILootBlockEntity;
import noobanidus.mods.lootr.api.inventory.ILootrInventory;
//?} else {
/*import noobanidus.mods.lootr.api.tile.ILootTile;
import noobanidus.mods.lootr.data.SpecialChestInventory;*/
//?}

public class LootrCompat {
    public static boolean isLootrContainer(BlockEntity be) {
        //? if >=1.18 {
        return be instanceof ILootBlockEntity;
        //?} else {
        /*return be instanceof ILootTile;*/
        //?}
    }

    public static boolean isLootrCart(AbstractMinecartContainer cart) {
        return cart instanceof LootrChestMinecartEntity;
    }

    public static int lootContainer(ServerPlayer player, BlockEntity be) {
        //? if >=1.18 {
        if (!(be instanceof ILootBlockEntity) || !(be instanceof RandomizableContainerBlockEntity)) {
            return -1;
        }
        ILootBlockEntity tile = (ILootBlockEntity) be;
        //?} else {
        /*if (!(be instanceof ILootTile) || !(be instanceof RandomizableContainerBlockEntity)) {
            return -1;
        }
        ILootTile tile = (ILootTile) be;
        *///?}
        RandomizableContainerBlockEntity rc = (RandomizableContainerBlockEntity) be;
        Level level = be.getLevel();
        if (level == null) {
            return -1;
        }
        //? if >=1.18 {
        ILootrInventory inventory = DataStorage.getInventory(
                level, tile.getTileId(), be.getBlockPos(), player, rc, tile::unpackLootTable);
        //?} else {
        /*SpecialChestInventory inventory = DataStorage.getInventory(
                level, tile.getTileId(), be.getBlockPos(), player, rc, tile::fillWithLoot);
        *///?}
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
        if (!(cart instanceof LootrChestMinecartEntity)) {
            return -1;
        }
        LootrChestMinecartEntity lootrCart = (LootrChestMinecartEntity) cart;
        //? if >=1.18 {
        ILootrInventory inventory = DataStorage.getInventory(
                Compat.level(cart), lootrCart, player, lootrCart::addLoot);
        //?} else {
        /*SpecialChestInventory inventory = DataStorage.getInventory(
                Compat.level(cart), lootrCart, player, lootrCart::addLoot, cart.blockPosition());
        *///?}
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
