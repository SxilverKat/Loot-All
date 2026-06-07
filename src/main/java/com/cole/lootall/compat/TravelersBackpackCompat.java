package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class TravelersBackpackCompat {
    private static boolean failed;
    private static boolean init;
    private static Class<?> cItem;
    private static Class<?> cTile;
    private static Constructor<?> ctorInventory;
    private static Method mSaveAllData;
    private static Item backpackItem;

    private TravelersBackpackCompat() {
    }

    private static boolean ensureInit() {
        if (failed) {
            return false;
        }
        if (init) {
            return true;
        }
        try {
            cItem = Class.forName("com.tiviacz.travelersbackpack.items.ItemTravelersBackpack");
            cTile = Class.forName("com.tiviacz.travelersbackpack.tileentity.TileEntityTravelersBackpack");
            Class<?> cInv = Class.forName("com.tiviacz.travelersbackpack.gui.inventory.InventoryTravelersBackpack");
            ctorInventory = cInv.getConstructor(ItemStack.class, EntityPlayer.class);
            mSaveAllData = cInv.getMethod("saveAllData", NBTTagCompound.class);
            for (Item item : ForgeRegistries.ITEMS) {
                if (cItem.isInstance(item)) {
                    backpackItem = item;
                    break;
                }
            }
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    public static boolean isBackpackItem(ItemStack stack) {
        if (!ensureInit() || stack.isEmpty()) {
            return false;
        }
        return cItem.isInstance(stack.getItem());
    }

    public static TransferService.ResolvedSink itemSink(EntityPlayer player, final ItemStack stack) {
        if (!isBackpackItem(stack)) {
            return null;
        }
        try {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                stack.setTagCompound(tag);
            }
            final NBTTagCompound theTag = tag;
            final Object inventory = ctorInventory.newInstance(stack, player);
            final IItemHandler handler = new InvWrapper((IInventory) inventory);
            TransferService.LootSink sink = new TransferService.LootSink() {
                @Override
                public ItemStack insert(ItemStack s) {
                    return ItemHandlerHelper.insertItemStacked(handler, s, false);
                }
            };
            Runnable writeBack = new Runnable() {
                @Override
                public void run() {
                    try {
                        mSaveAllData.invoke(inventory, theTag);
                    } catch (Throwable ignored) {
                    }
                }
            };
            return new TransferService.ResolvedSink(sink, TransferService.itemDisplayName(stack), writeBack);
        } catch (Throwable t) {
            return null;
        }
    }

    public static ITextComponent blockName(World level, BlockPos pos) {
        if (!ensureInit() || backpackItem == null) {
            return null;
        }
        TileEntity te = level.getTileEntity(pos);
        if (te != null && cTile.isInstance(te)) {
            return new TextComponentString(new ItemStack(backpackItem).getDisplayName());
        }
        return null;
    }

    public static TransferService.ResolvedSink blockSink(World level, BlockPos pos) {
        if (!ensureInit()) {
            return null;
        }
        try {
            TileEntity te = level.getTileEntity(pos);
            if (te == null || !cTile.isInstance(te)) {
                return null;
            }
            final IItemHandler handler = new InvWrapper((IInventory) te);
            TransferService.LootSink sink = new TransferService.LootSink() {
                @Override
                public ItemStack insert(ItemStack s) {
                    return ItemHandlerHelper.insertItemStacked(handler, s, false);
                }
            };
            ITextComponent name = backpackItem != null
                    ? new TextComponentString(new ItemStack(backpackItem).getDisplayName())
                    : TransferService.blockDisplayName(level, pos);
            return new TransferService.ResolvedSink(sink, name);
        } catch (Throwable t) {
            return null;
        }
    }
}
