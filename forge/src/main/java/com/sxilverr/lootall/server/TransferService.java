package com.sxilverr.lootall.server;
import com.sxilverr.lootall.core.TransferData;

import com.sxilverr.lootall.config.LootConfig;

import com.sxilverr.lootall.compat.AppliedEnergisticsCompat;
import com.sxilverr.lootall.compat.CuriosCompat;
import com.sxilverr.lootall.compat.MekanismCompat;
import com.sxilverr.lootall.compat.PrettyPipesCompat;
import com.sxilverr.lootall.compat.ProjectECompat;
import com.sxilverr.lootall.compat.RefinedStorageCompat;
import com.sxilverr.lootall.compat.SimpleStorageNetworkCompat;
import com.sxilverr.lootall.compat.TomsStorageCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TransferService {
    private static final boolean RS = ModList.get().isLoaded("refinedstorage");
    private static final boolean PE = ModList.get().isLoaded("projecte");
    private static final boolean AE2 = ModList.get().isLoaded("ae2");
    private static final boolean MEK = ModList.get().isLoaded("mekanism");
    private static final boolean TOMS = ModList.get().isLoaded("toms_storage");
    private static final boolean SSN = ModList.get().isLoaded("storagenetwork");
    private static final boolean PIPES = ModList.get().isLoaded("prettypipes");

    public interface LootSink {
        ItemStack insert(ItemStack stack);
    }

    public record ResolvedSink(LootSink sink, Component name, Runnable onComplete) {
        public ResolvedSink(LootSink sink, Component name) {
            this(sink, name, null);
        }
    }

    public static ResolvedSink resolveSink(ServerPlayer player) {
        if (!LootConfig.enableLootingTransfer) {
            return null;
        }
        if (!StageGate.canTransfer(player)) {
            return null;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }
        TransferData.Target target = TransferData.get(server).getTarget(player.getUUID());
        if (target instanceof TransferData.ItemTarget itemTarget) {
            return resolveItemSink(player, ForgeRegistries.ITEMS.getValue(itemTarget.item()));
        }
        if (!(target instanceof TransferData.BlockTarget block)) {
            return null;
        }
        ServerLevel targetLevel = server.getLevel(block.dimension());
        if (targetLevel == null) {
            return null;
        }
        boolean sameDimension = targetLevel.dimension() == player.level().dimension();
        if (LootConfig.transferRequireSameDimension && !sameDimension) {
            return null;
        }
        BlockPos pos = block.pos();
        if (sameDimension && LootConfig.maxLootTransferDistance > 0) {
            double maxSq = (double) LootConfig.maxLootTransferDistance * LootConfig.maxLootTransferDistance;
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxSq) {
                return null;
            }
        }
        if (RS) {
            LootSink rsSink = RefinedStorageCompat.blockSink(targetLevel, pos);
            if (rsSink != null) {
                return new ResolvedSink(rsSink, targetLevel.getBlockState(pos).getBlock().getName());
            }
        }
        ChunkPos chunkPos = new ChunkPos(pos);
        if (LootConfig.transferRequireLoadedChunk
                && targetLevel.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z) == null) {
            return null;
        }
        if (PE && ProjectECompat.isTransmutationTable(targetLevel.getBlockState(pos))) {
            LootSink emcSink = ProjectECompat.personalEmcSink(player);
            if (emcSink != null) {
                return new ResolvedSink(emcSink, targetLevel.getBlockState(pos).getBlock().getName(),
                        () -> ProjectECompat.syncPersonal(player));
            }
        }
        if (AE2) {
            LootSink ae2Sink = AppliedEnergisticsCompat.blockSink(targetLevel, pos, player);
            if (ae2Sink != null) {
                return new ResolvedSink(ae2Sink, Component.translatable("name.lootall.me_network"));
            }
        }
        if (MEK) {
            LootSink mekSink = MekanismCompat.blockSink(targetLevel, pos);
            if (mekSink != null) {
                return new ResolvedSink(mekSink, targetLevel.getBlockState(pos).getBlock().getName());
            }
        }
        if (TOMS) {
            LootSink tomsSink = TomsStorageCompat.blockSink(targetLevel, pos);
            if (tomsSink != null) {
                return new ResolvedSink(tomsSink, targetLevel.getBlockState(pos).getBlock().getName());
            }
        }
        if (SSN) {
            LootSink ssnSink = SimpleStorageNetworkCompat.blockSink(targetLevel, pos);
            if (ssnSink != null) {
                return new ResolvedSink(ssnSink, targetLevel.getBlockState(pos).getBlock().getName());
            }
        }
        if (PIPES) {
            LootSink pipesSink = PrettyPipesCompat.blockSink(targetLevel, pos);
            if (pipesSink != null) {
                return new ResolvedSink(pipesSink, targetLevel.getBlockState(pos).getBlock().getName());
            }
        }
        BlockEntity be = targetLevel.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        IItemHandler handler = findHandler(be);
        if (handler == null) {
            return null;
        }
        return new ResolvedSink(stack -> ItemHandlerHelper.insertItemStacked(handler, stack, false),
                targetLevel.getBlockState(pos).getBlock().getName());
    }

    private static ResolvedSink resolveItemSink(ServerPlayer player, Item item) {
        if (RS) {
            ItemStack networkStack = findPlayerStack(player, item);
            if (networkStack != null && RefinedStorageCompat.isNetworkItem(networkStack)) {
                LootSink rsSink = RefinedStorageCompat.itemSink(player, networkStack);
                if (rsSink != null) {
                    return new ResolvedSink(rsSink, networkStack.getHoverName());
                }
            }
        }
        if (PE) {
            ItemStack holderStack = findPlayerStack(player, item);
            if (holderStack != null && ProjectECompat.isEmcHolder(holderStack)) {
                LootSink emcSink = ProjectECompat.emcSink(holderStack);
                if (emcSink != null) {
                    return new ResolvedSink(emcSink, holderStack.getHoverName());
                }
            }
            if (ProjectECompat.isTransmutationTablet(item)) {
                LootSink emcSink = ProjectECompat.personalEmcSink(player);
                if (emcSink != null) {
                    return new ResolvedSink(emcSink, new ItemStack(item).getHoverName(),
                            () -> ProjectECompat.syncPersonal(player));
                }
            }
        }
        if (MEK) {
            ItemStack qioStack = findPlayerStack(player, item);
            if (qioStack != null && MekanismCompat.isQioItem(qioStack)) {
                LootSink mekSink = MekanismCompat.itemSink(qioStack);
                if (mekSink != null) {
                    return new ResolvedSink(mekSink, qioStack.getHoverName());
                }
            }
        }
        IItemHandler handler = resolveItemHandler(player, item);
        if (handler == null) {
            return null;
        }
        return new ResolvedSink(stack -> ItemHandlerHelper.insertItemStacked(handler, stack, false),
                new ItemStack(item).getHoverName());
    }

    public static IItemHandler resolveItemHandler(ServerPlayer player, Item item) {
        IItemHandler handler = inventoryItemHandler(player, item);
        if (handler == null) {
            handler = CuriosCompat.findItemHandler(player, item);
        }
        return handler;
    }

    public static boolean canTargetBlock(ServerLevel level, BlockPos pos) {
        if (RS && RefinedStorageCompat.blockSink(level, pos) != null) {
            return true;
        }
        if (PE && ProjectECompat.isTransmutationTable(level.getBlockState(pos))) {
            return true;
        }
        if (AE2 && AppliedEnergisticsCompat.hasNetwork(level, pos)) {
            return true;
        }
        if (MEK && MekanismCompat.blockSink(level, pos) != null) {
            return true;
        }
        if (TOMS && TomsStorageCompat.blockSink(level, pos) != null) {
            return true;
        }
        if (SSN && SimpleStorageNetworkCompat.blockSink(level, pos) != null) {
            return true;
        }
        if (PIPES && PrettyPipesCompat.blockSink(level, pos) != null) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && findHandler(be) != null;
    }

    public static boolean canTargetItem(ServerPlayer player, Item item) {
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
        if (MEK) {
            ItemStack stack = findPlayerStack(player, item);
            if (stack != null && MekanismCompat.isQioItem(stack)) {
                return true;
            }
        }
        return resolveItemHandler(player, item) != null;
    }

    private static ItemStack findPlayerStack(ServerPlayer player, Item item) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return stack;
            }
        }
        return null;
    }

    private static IItemHandler inventoryItemHandler(ServerPlayer player, Item item) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

    public static IItemHandler findHandler(BlockEntity be) {
        IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
        if (handler != null) {
            return handler;
        }
        for (Direction direction : Direction.values()) {
            handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).resolve().orElse(null);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
}
