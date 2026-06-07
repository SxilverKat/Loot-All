package com.cole.lootall.server;

import com.cole.lootall.Config;
import com.cole.lootall.compat.AppliedEnergisticsCompat;
import com.cole.lootall.compat.BaublesCompat;
import com.cole.lootall.compat.IronBackpacksCompat;
import com.cole.lootall.compat.ProjectECompat;
import com.cole.lootall.compat.RefinedStorageCompat;
import com.cole.lootall.compat.SimpleStorageNetworkCompat;
import com.cole.lootall.compat.TravelersBackpackCompat;
import com.cole.lootall.compat.WearableBackpacksCompat;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public final class TransferService {
    private static final boolean RS = Loader.isModLoaded("refinedstorage");
    private static final boolean PE = Loader.isModLoaded("projecte");
    private static final boolean AE2 = Loader.isModLoaded("appliedenergistics2");
    private static final boolean SSN = Loader.isModLoaded("storagenetwork");

    private TransferService() {
    }

    public interface LootSink {
        ItemStack insert(ItemStack stack);
    }

    public static final class ResolvedSink {
        public final LootSink sink;
        public final ITextComponent name;
        public final Runnable onComplete;

        public ResolvedSink(LootSink sink, ITextComponent name) {
            this(sink, name, null);
        }

        public ResolvedSink(LootSink sink, ITextComponent name, Runnable onComplete) {
            this.sink = sink;
            this.name = name;
            this.onComplete = onComplete;
        }
    }

    public static ResolvedSink resolveSink(EntityPlayerMP player) {
        if (!Config.enableLootingTransfer) {
            return null;
        }
        if (!StageGate.canTransfer(player)) {
            return null;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }
        TransferData.Target target = TransferData.get(server).getTarget(player.getUniqueID());
        if (target == null) {
            return null;
        }
        if (target.isItem) {
            Item item = ForgeRegistries.ITEMS.getValue(target.item);
            return item == null ? null : resolveItemSink(player, item);
        }

        WorldServer targetLevel = server.getWorld(target.dimension);
        if (targetLevel == null) {
            return null;
        }
        boolean sameDimension = target.dimension == player.dimension;
        if (Config.transferRequireSameDimension && !sameDimension) {
            return null;
        }
        final BlockPos pos = target.pos;
        if (sameDimension && Config.maxLootTransferDistance > 0) {
            double maxSq = (double) Config.maxLootTransferDistance * Config.maxLootTransferDistance;
            if (player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxSq) {
                return null;
            }
        }

        if (RS) {
            LootSink rsSink = RefinedStorageCompat.blockSink(targetLevel, pos);
            if (rsSink != null) {
                return new ResolvedSink(rsSink, blockDisplayName(targetLevel, pos));
            }
        }
        if (Config.transferRequireLoadedChunk
                && targetLevel.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) == null) {
            return null;
        }
        if (PE && ProjectECompat.isTransmutationTable(targetLevel.getBlockState(pos))) {
            LootSink emcSink = ProjectECompat.personalEmcSink(player);
            if (emcSink != null) {
                final EntityPlayerMP syncPlayer = player;
                return new ResolvedSink(emcSink, blockDisplayName(targetLevel, pos), new Runnable() {
                    @Override
                    public void run() {
                        ProjectECompat.syncPersonal(syncPlayer);
                    }
                });
            }
        }
        if (AE2) {
            LootSink ae2Sink = AppliedEnergisticsCompat.blockSink(targetLevel, pos, player);
            if (ae2Sink != null) {
                return new ResolvedSink(ae2Sink, new TextComponentTranslation("name.lootall.me_network"));
            }
        }
        if (SSN) {
            LootSink ssnSink = SimpleStorageNetworkCompat.blockSink(targetLevel, pos);
            if (ssnSink != null) {
                return new ResolvedSink(ssnSink, blockDisplayName(targetLevel, pos));
            }
        }
        ResolvedSink travelBlock = TravelersBackpackCompat.blockSink(targetLevel, pos);
        if (travelBlock != null) {
            return travelBlock;
        }
        ResolvedSink wearBlock = WearableBackpacksCompat.blockSink(targetLevel, pos);
        if (wearBlock != null) {
            return wearBlock;
        }

        TileEntity be = targetLevel.getTileEntity(pos);
        if (be == null) {
            return null;
        }
        final IItemHandler handler = findHandler(be);
        if (handler == null) {
            return null;
        }
        return new ResolvedSink(new LootSink() {
            @Override
            public ItemStack insert(ItemStack stack) {
                return ItemHandlerHelper.insertItemStacked(handler, stack, false);
            }
        }, blockDisplayName(targetLevel, pos));
    }

    private static ResolvedSink resolveItemSink(EntityPlayerMP player, Item item) {
        if (RS) {
            ItemStack networkStack = findPlayerStack(player, item);
            if (networkStack != null && RefinedStorageCompat.isNetworkItem(networkStack)) {
                LootSink rsSink = RefinedStorageCompat.itemSink(player, networkStack);
                if (rsSink != null) {
                    return new ResolvedSink(rsSink, itemDisplayName(networkStack));
                }
            }
        }
        if (PE) {
            ItemStack holderStack = findPlayerStack(player, item);
            if (holderStack != null && ProjectECompat.isEmcHolder(holderStack)) {
                LootSink emcSink = ProjectECompat.emcSink(holderStack);
                if (emcSink != null) {
                    return new ResolvedSink(emcSink, itemDisplayName(holderStack));
                }
            }
            if (ProjectECompat.isTransmutationTablet(item)) {
                LootSink emcSink = ProjectECompat.personalEmcSink(player);
                if (emcSink != null) {
                    final EntityPlayerMP syncPlayer = player;
                    return new ResolvedSink(emcSink, itemDisplayName(new ItemStack(item)), new Runnable() {
                        @Override
                        public void run() {
                            ProjectECompat.syncPersonal(syncPlayer);
                        }
                    });
                }
            }
        }
        ItemStack heldStack = findPlayerStack(player, item);
        if (heldStack != null) {
            ResolvedSink ironbp = IronBackpacksCompat.itemSink(heldStack);
            if (ironbp != null) {
                return ironbp;
            }
            ResolvedSink travel = TravelersBackpackCompat.itemSink(player, heldStack);
            if (travel != null) {
                return travel;
            }
            if (AE2) {
                LootSink wireless = AppliedEnergisticsCompat.wirelessItemSink(player, heldStack);
                if (wireless != null) {
                    return new ResolvedSink(wireless, new TextComponentTranslation("name.lootall.me_network"));
                }
            }
        }
        final IItemHandler handler = resolveItemHandler(player, item);
        if (handler == null) {
            return null;
        }
        return new ResolvedSink(new LootSink() {
            @Override
            public ItemStack insert(ItemStack stack) {
                return ItemHandlerHelper.insertItemStacked(handler, stack, false);
            }
        }, itemDisplayName(new ItemStack(item)));
    }

    public static IItemHandler resolveItemHandler(EntityPlayerMP player, Item item) {
        IItemHandler handler = inventoryItemHandler(player, item);
        if (handler == null) {
            handler = BaublesCompat.findItemHandler(player, item);
        }
        return handler;
    }

    public static boolean canTargetBlock(WorldServer level, BlockPos pos) {
        if (RS && RefinedStorageCompat.blockSink(level, pos) != null) {
            return true;
        }
        if (PE && ProjectECompat.isTransmutationTable(level.getBlockState(pos))) {
            return true;
        }
        if (AE2 && AppliedEnergisticsCompat.hasNetwork(level, pos)) {
            return true;
        }
        if (SSN && SimpleStorageNetworkCompat.blockSink(level, pos) != null) {
            return true;
        }
        if (TravelersBackpackCompat.blockSink(level, pos) != null) {
            return true;
        }
        if (WearableBackpacksCompat.blockSink(level, pos) != null) {
            return true;
        }
        TileEntity be = level.getTileEntity(pos);
        return be != null && findHandler(be) != null;
    }

    public static boolean canTargetItem(EntityPlayerMP player, Item item) {
        if (RS) {
            ItemStack stack = findPlayerStack(player, item);
            if (stack != null && RefinedStorageCompat.isNetworkItem(stack)) {
                return true;
            }
        }
        if (PE) {
            ItemStack stack = findPlayerStack(player, item);
            if (stack != null && ProjectECompat.isEmcHolder(stack)) {
                return true;
            }
            if (ProjectECompat.isTransmutationTablet(item)) {
                return true;
            }
        }
        ItemStack heldStack = findPlayerStack(player, item);
        if (heldStack != null) {
            if (IronBackpacksCompat.isBackpack(heldStack)) {
                return true;
            }
            if (TravelersBackpackCompat.isBackpackItem(heldStack)) {
                return true;
            }
            if (AE2 && AppliedEnergisticsCompat.isWirelessTerminal(heldStack)) {
                return true;
            }
        }
        return resolveItemHandler(player, item) != null;
    }

    private static ItemStack findPlayerStack(EntityPlayer player, Item item) {
        InventoryPlayer inventory = player.inventory;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return stack;
            }
        }
        return null;
    }

    private static IItemHandler inventoryItemHandler(EntityPlayer player, Item item) {
        InventoryPlayer inventory = player.inventory;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

    public static IItemHandler findHandler(TileEntity be) {
        IItemHandler handler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (handler != null) {
            return handler;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            handler = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    public static ITextComponent blockTargetName(World world, BlockPos pos) {
        ITextComponent travel = TravelersBackpackCompat.blockName(world, pos);
        if (travel != null) {
            return travel;
        }
        return blockDisplayName(world, pos);
    }

    public static ITextComponent blockDisplayName(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        ItemStack stack = blockToStack(world, pos, state);
        if (!stack.isEmpty()) {
            return new TextComponentString(stack.getDisplayName());
        }
        return new TextComponentString(state.getBlock().getLocalizedName());
    }

    public static ITextComponent itemDisplayName(ItemStack stack) {
        if (stack.isEmpty()) {
            return new TextComponentString("");
        }
        return new TextComponentString(stack.getDisplayName());
    }

    public static ITextComponent itemTargetName(EntityPlayerMP player, Item item) {
        ItemStack stack = findPlayerStack(player, item);
        if (stack != null && !stack.isEmpty()) {
            return itemDisplayName(stack);
        }
        return itemDisplayName(new ItemStack(item));
    }

    private static ItemStack blockToStack(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        try {
            ItemStack pick = block.getPickBlock(state, null, world, pos, null);
            if (pick != null && !pick.isEmpty()) {
                return pick;
            }
        } catch (Throwable ignored) {
        }
        Item item = Item.getItemFromBlock(block);
        if (item != Items.AIR) {
            return new ItemStack(item, 1, block.damageDropped(state));
        }
        return ItemStack.EMPTY;
    }
}
