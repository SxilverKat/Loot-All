package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.server.TransferService;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.item.NetworkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RefinedStorageCompat {
    public static boolean isNetworkItem(ItemStack stack) {
        return stack.getItem() instanceof NetworkItem && NetworkItem.isValid(stack);
    }

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        INetworkNode node = API.instance().getNetworkNodeManager(level).getNode(pos);
        if (node == null) {
            return null;
        }
        INetwork network = node.getNetwork();
        return network == null ? null : networkSink(network);
    }

    public static TransferService.LootSink itemSink(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof NetworkItem networkItem) || !NetworkItem.isValid(stack)) {
            return null;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }
        INetwork[] holder = new INetwork[1];
        networkItem.applyNetwork(server, stack, network -> holder[0] = network, error -> {
        });
        return holder[0] == null ? null : networkSink(holder[0]);
    }

    private static TransferService.LootSink networkSink(INetwork network) {
        return stack -> {
            try {
                return network.insertItem(stack, stack.getCount(), Action.PERFORM);
            } catch (Exception e) {
                return stack;
            }
        };
    }
}
