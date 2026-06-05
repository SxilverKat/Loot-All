package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.math.BigInteger;

public class ProjectECompat {
    private static final ResourceLocation TRANSMUTATION_TABLE = new ResourceLocation("projecte", "transmutation_table");
    private static final ResourceLocation TRANSMUTATION_TABLET = new ResourceLocation("projecte", "transmutation_tablet");

    public static boolean isEmcHolder(ItemStack stack) {
        return stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).isPresent();
    }

    public static boolean isTransmutationTablet(Item item) {
        return TRANSMUTATION_TABLET.equals(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isTransmutationTable(BlockState state) {
        return TRANSMUTATION_TABLE.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    public static TransferService.LootSink emcSink(ItemStack holderStack) {
        IItemEmcHolder holder = holderStack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY)
                .resolve().orElse(null);
        if (holder == null) {
            return null;
        }
        return stack -> {
            long emcPerItem = ProjectEAPI.getEMCProxy().getValue(stack);
            if (emcPerItem <= 0) {
                return stack;
            }
            long totalEmc = emcPerItem * stack.getCount();
            long insertable = holder.insertEmc(holderStack, totalEmc, IEmcStorage.EmcAction.SIMULATE);
            int converted = (int) Math.min(stack.getCount(), insertable / emcPerItem);
            if (converted <= 0) {
                return stack;
            }
            holder.insertEmc(holderStack, (long) converted * emcPerItem, IEmcStorage.EmcAction.EXECUTE);
            if (converted >= stack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - converted);
            return remainder;
        };
    }

    public static TransferService.LootSink personalEmcSink(ServerPlayer player) {
        IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY)
                .resolve().orElse(null);
        if (provider == null) {
            return null;
        }
        return stack -> {
            long emcPerItem = ProjectEAPI.getEMCProxy().getValue(stack);
            if (emcPerItem <= 0) {
                return stack;
            }
            provider.addKnowledge(stack);
            BigInteger added = BigInteger.valueOf(emcPerItem).multiply(BigInteger.valueOf(stack.getCount()));
            provider.setEmc(provider.getEmc().add(added));
            return ItemStack.EMPTY;
        };
    }

    public static void syncPersonal(ServerPlayer player) {
        IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY)
                .resolve().orElse(null);
        if (provider != null) {
            provider.sync(player);
        }
    }
}
