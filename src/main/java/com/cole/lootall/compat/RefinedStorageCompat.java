package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.slotreference.InventorySlotReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RefinedStorageCompat {
    public static boolean isNetworkItem(ItemStack stack) {
        return RefinedStorageApi.INSTANCE.getNetworkItemHelper().isBound(stack);
    }

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AbstractNetworkNodeContainerBlockEntity<?> containerBe)) {
            return null;
        }
        Network network = resolveNetwork(containerBe);
        return network == null ? null : networkSink(network);
    }

    public static TransferService.LootSink itemSink(ServerPlayer player, ItemStack stack) {
        NetworkItemHelper helper = RefinedStorageApi.INSTANCE.getNetworkItemHelper();
        if (!helper.isBound(stack)) {
            return null;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() == stack.getItem() && helper.isBound(held)) {
                SlotReference reference = InventorySlotReference.of(player, hand);
                NetworkItemContext context = helper.createContext(held, player, reference);
                Network network = context.resolveNetwork().orElse(null);
                if (network != null) {
                    return networkSink(network);
                }
            }
        }
        return null;
    }

    private static Network resolveNetwork(AbstractNetworkNodeContainerBlockEntity<?> be) {
        for (InWorldNetworkNodeContainer container : be.getContainerProvider().getContainers()) {
            NetworkNode node = container.getNode();
            if (node != null && node.getNetwork() != null) {
                return node.getNetwork();
            }
        }
        return null;
    }

    private static TransferService.LootSink networkSink(Network network) {
        StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        if (storage == null) {
            return null;
        }
        return stack -> {
            try {
                ItemResource resource = ItemResource.ofItemStack(stack);
                long inserted = storage.insert(resource, stack.getCount(), Action.EXECUTE, Actor.EMPTY);
                if (inserted <= 0) {
                    return stack;
                }
                if (inserted >= stack.getCount()) {
                    return ItemStack.EMPTY;
                }
                ItemStack remainder = stack.copy();
                remainder.setCount(stack.getCount() - (int) inserted);
                return remainder;
            } catch (Exception e) {
                return stack;
            }
        };
    }
}
