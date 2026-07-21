package com.sxilverr.lootall;

//? if >=1.19 {
import net.minecraftforge.common.capabilities.ForgeCapabilities;
//?} else {
/*import net.minecraftforge.items.CapabilityItemHandler;*/
//?}
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class Compat {
    private Compat() {
    }

    public static Level level(Entity entity) {
        //? if >=1.20 {
        return entity.level();
        //?} else {
        /*return entity.level;*/
        //?}
    }

    public static Inventory inventory(Player player) {
        //? if >=1.18 {
        return player.getInventory();
        //?} else {
        /*return player.inventory;*/
        //?}
    }

    public static Capability<IItemHandler> itemHandlerCap() {
        //? if >=1.19 {
        return ForgeCapabilities.ITEM_HANDLER;
        //?} else {
        /*return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;*/
        //?}
    }
}
