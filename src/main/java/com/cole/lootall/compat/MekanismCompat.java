package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import mekanism.api.Action;
import mekanism.api.inventory.qio.IQIOComponent;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Mekanism QIO (Quantum Item Orchestration) loot-transfer support.
 *
 * <p>QIO stores items in a frequency-based network ({@link IQIOFrequency}), not a vanilla
 * {@code IItemHandler}, so it needs dedicated handling like Refined Storage. Any QIO block
 * (Dashboard, Drive Array, Importer, Exporter) shares the network frequency via the public
 * API {@link IQIOComponent}, so a single instanceof check covers them all. The Portable QIO
 * Dashboard item resolves its frequency through {@link IFrequencyItem}, giving a wireless
 * item-target sink analogous to the RS wireless grid.
 */
public class MekanismCompat {

    public static boolean isQioItem(ItemStack stack) {
        return itemFrequency(stack) != null;
    }

    public static TransferService.LootSink blockSink(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IQIOComponent component)) {
            return null;
        }
        return frequencySink(component.getQIOFrequency());
    }

    public static TransferService.LootSink itemSink(ItemStack stack) {
        return frequencySink(itemFrequency(stack));
    }

    private static IQIOFrequency itemFrequency(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IFrequencyItem freqItem)) {
            return null;
        }
        try {
            if (!freqItem.hasFrequency(stack)) {
                return null;
            }
            Frequency frequency = freqItem.getFrequency(stack);
            if (frequency instanceof IQIOFrequency qio && qio.isValid()) {
                return qio;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static TransferService.LootSink frequencySink(IQIOFrequency frequency) {
        if (frequency == null || !frequency.isValid()) {
            return null;
        }
        return stack -> {
            try {
                long count = stack.getCount();
                // massInsert returns the amount actually inserted (verified via bytecode:
                // amount - QIOItemTypeData.add leftover); capacity guards return 0 when full.
                long inserted = frequency.massInsert(stack, count, Action.EXECUTE);
                if (inserted <= 0) {
                    return stack;
                }
                if (inserted >= count) {
                    return ItemStack.EMPTY;
                }
                ItemStack remainder = stack.copy();
                remainder.setCount((int) (count - inserted));
                return remainder;
            } catch (Exception e) {
                return stack;
            }
        };
    }
}
