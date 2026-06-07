package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class RefinedStorageCompat {
    private static boolean failed;
    private static boolean init;
    private static Method mApiInstance;
    private static Method mGetNodeManager;
    private static Method mGetNode;
    private static Method mGetNetwork;
    private static Method mInsertItem;
    private static Object actionPerform;
    private static Class<?> cItemNetworkItem;
    private static Method mApplyNetwork;

    private RefinedStorageCompat() {
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
            Class<?> cApi = Class.forName("com.raoulvdberge.refinedstorage.apiimpl.API");
            mApiInstance = cApi.getMethod("instance");
            Class<?> cIRSAPI = Class.forName("com.raoulvdberge.refinedstorage.api.IRSAPI");
            mGetNodeManager = cIRSAPI.getMethod("getNetworkNodeManager", World.class);
            Class<?> cNodeManager = Class.forName("com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeManager");
            mGetNode = cNodeManager.getMethod("getNode", BlockPos.class);
            Class<?> cNode = Class.forName("com.raoulvdberge.refinedstorage.api.network.node.INetworkNode");
            mGetNetwork = cNode.getMethod("getNetwork");
            Class<?> cNetwork = Class.forName("com.raoulvdberge.refinedstorage.api.network.INetwork");
            Class<?> cAction = Class.forName("com.raoulvdberge.refinedstorage.api.util.Action");
            mInsertItem = cNetwork.getMethod("insertItem", ItemStack.class, int.class, cAction);
            actionPerform = Enum.valueOf((Class) cAction, "PERFORM");
            cItemNetworkItem = Class.forName("com.raoulvdberge.refinedstorage.item.ItemNetworkItem");
            mApplyNetwork = cItemNetworkItem.getMethod("applyNetwork", ItemStack.class, Consumer.class, Consumer.class);
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    public static boolean isNetworkItem(ItemStack stack) {
        if (!ensureInit() || stack.isEmpty()) {
            return false;
        }
        return cItemNetworkItem.isInstance(stack.getItem());
    }

    public static TransferService.LootSink blockSink(World level, BlockPos pos) {
        if (!ensureInit()) {
            return null;
        }
        try {
            Object api = mApiInstance.invoke(null);
            Object manager = mGetNodeManager.invoke(api, level);
            if (manager == null) {
                return null;
            }
            Object node = mGetNode.invoke(manager, pos);
            if (node == null) {
                return null;
            }
            Object network = mGetNetwork.invoke(node);
            return network == null ? null : networkSink(network);
        } catch (Throwable t) {
            return null;
        }
    }

    public static TransferService.LootSink itemSink(EntityPlayerMP player, ItemStack stack) {
        if (!ensureInit() || !cItemNetworkItem.isInstance(stack.getItem())) {
            return null;
        }
        try {
            final Object[] holder = new Object[1];
            Consumer<Object> networkConsumer = new Consumer<Object>() {
                @Override
                public void accept(Object network) {
                    holder[0] = network;
                }
            };
            Consumer<Object> errorConsumer = new Consumer<Object>() {
                @Override
                public void accept(Object error) {
                }
            };
            mApplyNetwork.invoke(stack.getItem(), stack, networkConsumer, errorConsumer);
            return holder[0] == null ? null : networkSink(holder[0]);
        } catch (Throwable t) {
            return null;
        }
    }

    private static TransferService.LootSink networkSink(final Object network) {
        return new TransferService.LootSink() {
            @Override
            public ItemStack insert(ItemStack stack) {
                try {
                    Object remainder = mInsertItem.invoke(network, stack, stack.getCount(), actionPerform);
                    return remainder instanceof ItemStack ? (ItemStack) remainder : ItemStack.EMPTY;
                } catch (Throwable t) {
                    return stack;
                }
            }
        };
    }
}
