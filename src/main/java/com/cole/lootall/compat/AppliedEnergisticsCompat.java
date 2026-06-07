package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class AppliedEnergisticsCompat {
    private static boolean failed;
    private static boolean init;
    private static Class<?> cIGridHost;
    private static Class<?> cIActionHost;
    private static Class<?> cStorageGrid;
    private static Method mGetGridNode;
    private static Method mGetGrid;
    private static Method mGetCache;
    private static Method mGetInventory;
    private static Method mInjectItems;
    private static Method mCreateStack;
    private static Method mGetStackSize;
    private static Object partInternal;
    private static Object[] partValues;
    private static Object actionableModulate;
    private static Object storageChannel;
    private static Constructor<?> ctorPlayerSource;

    private static boolean wirelessFailed;
    private static boolean wirelessInit;
    private static Object aeApiInstance;
    private static Method mRegistries;
    private static Method mWireless;
    private static Method mLocatable;
    private static Method mIsWirelessTerminal;
    private static Method mGetWirelessHandler;
    private static Method mGetEncryptionKey;
    private static Method mGetLocatableBy;
    private static Method mGetActionableNode;

    private AppliedEnergisticsCompat() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean ensureInit() {
        if (failed) {
            return false;
        }
        if (init) {
            return true;
        }
        try {
            cIGridHost = Class.forName("appeng.api.networking.IGridHost");
            cIActionHost = Class.forName("appeng.api.networking.security.IActionHost");
            Class<?> cPart = Class.forName("appeng.api.util.AEPartLocation");
            partInternal = Enum.valueOf((Class) cPart, "INTERNAL");
            partValues = (Object[]) cPart.getMethod("values").invoke(null);
            mGetGridNode = cIGridHost.getMethod("getGridNode", cPart);
            Class<?> cNode = Class.forName("appeng.api.networking.IGridNode");
            mGetGrid = cNode.getMethod("getGrid");
            Class<?> cGrid = Class.forName("appeng.api.networking.IGrid");
            mGetCache = cGrid.getMethod("getCache", Class.class);
            cStorageGrid = Class.forName("appeng.api.networking.storage.IStorageGrid");
            Class<?> cStorageChannelIface = Class.forName("appeng.api.storage.IStorageChannel");
            Class<?> cMonitorable = Class.forName("appeng.api.storage.IStorageMonitorable");
            mGetInventory = cMonitorable.getMethod("getInventory", cStorageChannelIface);
            Class<?> cMEInventory = Class.forName("appeng.api.storage.IMEInventory");
            Class<?> cIAEStack = Class.forName("appeng.api.storage.data.IAEStack");
            Class<?> cActionable = Class.forName("appeng.api.config.Actionable");
            Class<?> cIActionSource = Class.forName("appeng.api.networking.security.IActionSource");
            mInjectItems = cMEInventory.getMethod("injectItems", cIAEStack, cActionable, cIActionSource);
            mCreateStack = cStorageChannelIface.getMethod("createStack", Object.class);
            mGetStackSize = cIAEStack.getMethod("getStackSize");
            actionableModulate = Enum.valueOf((Class) cActionable, "MODULATE");
            Class<?> cIItemStorageChannel = Class.forName("appeng.api.storage.channels.IItemStorageChannel");
            Class<?> cAEApi = Class.forName("appeng.api.AEApi");
            Object aeApi = cAEApi.getMethod("instance").invoke(null);
            aeApiInstance = aeApi;
            Class<?> cIAppEngApi = Class.forName("appeng.api.IAppEngApi");
            Object storageHelper = cIAppEngApi.getMethod("storage").invoke(aeApi);
            Class<?> cIStorageHelper = Class.forName("appeng.api.storage.IStorageHelper");
            Method mGetStorageChannel = cIStorageHelper.getMethod("getStorageChannel", Class.class);
            storageChannel = mGetStorageChannel.invoke(storageHelper, cIItemStorageChannel);
            Class<?> cPlayerSource = Class.forName("appeng.me.helpers.PlayerSource");
            ctorPlayerSource = cPlayerSource.getConstructor(EntityPlayer.class, cIActionHost);
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    private static boolean ensureWireless() {
        if (wirelessFailed) {
            return false;
        }
        if (wirelessInit) {
            return true;
        }
        if (!ensureInit()) {
            wirelessFailed = true;
            return false;
        }
        try {
            Class<?> cIAppEngApi = Class.forName("appeng.api.IAppEngApi");
            mRegistries = cIAppEngApi.getMethod("registries");
            Class<?> cRegistryContainer = Class.forName("appeng.api.features.IRegistryContainer");
            mWireless = cRegistryContainer.getMethod("wireless");
            mLocatable = cRegistryContainer.getMethod("locatable");
            Class<?> cWirelessReg = Class.forName("appeng.api.features.IWirelessTermRegistry");
            mIsWirelessTerminal = cWirelessReg.getMethod("isWirelessTerminal", ItemStack.class);
            mGetWirelessHandler = cWirelessReg.getMethod("getWirelessTerminalHandler", ItemStack.class);
            Class<?> cWirelessHandler = Class.forName("appeng.api.features.IWirelessTermHandler");
            mGetEncryptionKey = cWirelessHandler.getMethod("getEncryptionKey", ItemStack.class);
            Class<?> cLocatableReg = Class.forName("appeng.api.features.ILocatableRegistry");
            mGetLocatableBy = cLocatableReg.getMethod("getLocatableBy", long.class);
            mGetActionableNode = cIActionHost.getMethod("getActionableNode");
            wirelessInit = true;
            return true;
        } catch (Throwable t) {
            wirelessFailed = true;
            return false;
        }
    }

    public static boolean hasNetwork(World level, BlockPos pos) {
        if (!ensureInit()) {
            return false;
        }
        try {
            return resolveMonitor(level, pos) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public static TransferService.LootSink blockSink(World level, BlockPos pos, EntityPlayer player) {
        if (!ensureInit()) {
            return null;
        }
        try {
            TileEntity te = level.getTileEntity(pos);
            Object monitor = resolveMonitor(level, pos);
            if (monitor == null) {
                return null;
            }
            Object host = (te != null && cIActionHost.isInstance(te)) ? te : null;
            Object source = ctorPlayerSource.newInstance(player, host);
            return meSink(monitor, source);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean isWirelessTerminal(ItemStack stack) {
        if (!ensureWireless() || stack.isEmpty()) {
            return false;
        }
        try {
            Object registries = mRegistries.invoke(aeApiInstance);
            Object wireless = mWireless.invoke(registries);
            Object result = mIsWirelessTerminal.invoke(wireless, stack);
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable t) {
            return false;
        }
    }

    public static TransferService.LootSink wirelessItemSink(EntityPlayer player, ItemStack stack) {
        if (!ensureWireless()) {
            return null;
        }
        try {
            Object registries = mRegistries.invoke(aeApiInstance);
            Object wireless = mWireless.invoke(registries);
            Object handler = mGetWirelessHandler.invoke(wireless, stack);
            if (handler == null) {
                return null;
            }
            Object keyObj = mGetEncryptionKey.invoke(handler, stack);
            if (!(keyObj instanceof String) || ((String) keyObj).isEmpty()) {
                return null;
            }
            long serial;
            try {
                serial = Long.parseLong((String) keyObj);
            } catch (NumberFormatException e) {
                return null;
            }
            Object locatableReg = mLocatable.invoke(registries);
            Object located = mGetLocatableBy.invoke(locatableReg, serial);
            if (located == null || !cIActionHost.isInstance(located)) {
                return null;
            }
            Object node = mGetActionableNode.invoke(located);
            if (node == null) {
                return null;
            }
            Object grid = mGetGrid.invoke(node);
            if (grid == null) {
                return null;
            }
            Object storageGrid = mGetCache.invoke(grid, cStorageGrid);
            if (storageGrid == null) {
                return null;
            }
            Object monitor = mGetInventory.invoke(storageGrid, storageChannel);
            if (monitor == null) {
                return null;
            }
            Object source = ctorPlayerSource.newInstance(player, null);
            return meSink(monitor, source);
        } catch (Throwable t) {
            return null;
        }
    }

    private static TransferService.LootSink meSink(final Object monitor, final Object source) {
        return new TransferService.LootSink() {
            @Override
            public ItemStack insert(ItemStack stack) {
                try {
                    Object aeStack = mCreateStack.invoke(storageChannel, stack);
                    if (aeStack == null) {
                        return stack;
                    }
                    Object remainder = mInjectItems.invoke(monitor, aeStack, actionableModulate, source);
                    if (remainder == null) {
                        return ItemStack.EMPTY;
                    }
                    long left = (Long) mGetStackSize.invoke(remainder);
                    if (left >= stack.getCount()) {
                        return stack;
                    }
                    if (left <= 0) {
                        return ItemStack.EMPTY;
                    }
                    ItemStack rem = stack.copy();
                    rem.setCount((int) left);
                    return rem;
                } catch (Throwable t) {
                    return stack;
                }
            }
        };
    }

    private static Object resolveMonitor(World level, BlockPos pos) throws Exception {
        TileEntity te = level.getTileEntity(pos);
        if (te == null || !cIGridHost.isInstance(te)) {
            return null;
        }
        Object node = mGetGridNode.invoke(te, partInternal);
        if (node == null) {
            for (Object loc : partValues) {
                node = mGetGridNode.invoke(te, loc);
                if (node != null) {
                    break;
                }
            }
        }
        if (node == null) {
            return null;
        }
        Object grid = mGetGrid.invoke(node);
        if (grid == null) {
            return null;
        }
        Object storageGrid = mGetCache.invoke(grid, cStorageGrid);
        if (storageGrid == null) {
            return null;
        }
        return mGetInventory.invoke(storageGrid, storageChannel);
    }
}
