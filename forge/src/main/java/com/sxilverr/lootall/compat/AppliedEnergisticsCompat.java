package com.sxilverr.lootall.compat;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
//? if >=1.18 {
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import net.minecraft.core.Direction;
//?} else {
/*import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.capabilities.Capabilities;
import appeng.core.Api;
import appeng.me.helpers.PlayerSource;
import net.minecraft.world.level.block.entity.BlockEntity;*/
//?}
import com.sxilverr.lootall.server.TransferService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class AppliedEnergisticsCompat {
    //? if >=1.18 {
    public static boolean hasNetwork(ServerLevel level, BlockPos pos) {
        return resolveStorage(level, pos) != null;
    }

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos, ServerPlayer player) {
        MEStorage storage = resolveStorage(level, pos);
        if (storage == null) {
            return null;
        }
        IActionSource source = IActionSource.ofPlayer(player);
        return stack -> {
            AEItemKey key = AEItemKey.of(stack);
            if (key == null) {
                return stack;
            }
            long inserted = storage.insert(key, stack.getCount(), Actionable.MODULATE, source);
            if (inserted <= 0) {
                return stack;
            }
            if (inserted >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - (int) inserted);
            return remainder;
        };
    }

    private static MEStorage resolveStorage(ServerLevel level, BlockPos pos) {
        IInWorldGridNodeHost host = GridHelper.getNodeHost(level, pos);
        if (host == null) {
            return null;
        }
        IGridNode node = host.getGridNode(null);
        if (node == null) {
            for (Direction direction : Direction.values()) {
                node = host.getGridNode(direction);
                if (node != null) {
                    break;
                }
            }
        }
        if (node == null) {
            return null;
        }
        IGrid grid = node.getGrid();
        if (grid == null) {
            return null;
        }
        IStorageService storageService = grid.getStorageService();
        return storageService == null ? null : storageService.getInventory();
    }
    //?} else {
    /*public static boolean hasNetwork(ServerLevel level, BlockPos pos) {
        return accessor(level, pos) != null;
    }

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos, ServerPlayer player) {
        IStorageMonitorableAccessor accessor = accessor(level, pos);
        if (accessor == null) {
            return null;
        }
        IActionSource source = new PlayerSource(player, null);
        IStorageMonitorable monitorable = accessor.getInventory(source);
        if (monitorable == null) {
            return null;
        }
        IItemStorageChannel channel = Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
        IMEMonitor<IAEItemStack> monitor = monitorable.getInventory(channel);
        if (monitor == null) {
            return null;
        }
        return stack -> {
            IAEItemStack toInsert = channel.createStack(stack);
            if (toInsert == null) {
                return stack;
            }
            IAEItemStack remainder = monitor.injectItems(toInsert, Actionable.MODULATE, source);
            if (remainder == null || remainder.getStackSize() <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack rem = remainder.createItemStack();
            return rem == null ? ItemStack.EMPTY : rem;
        };
    }

    private static IStorageMonitorableAccessor accessor(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        return be.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR).orElse(null);
    }*/
    //?}
}
