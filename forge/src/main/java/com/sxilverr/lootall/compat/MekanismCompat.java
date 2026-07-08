package com.sxilverr.lootall.compat;

import com.sxilverr.lootall.server.TransferService;
import mekanism.api.Action;
import mekanism.api.inventory.qio.IQIOComponent;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

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
