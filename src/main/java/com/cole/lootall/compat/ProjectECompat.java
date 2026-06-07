package com.cole.lootall.compat;

import com.cole.lootall.server.TransferService;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ProjectECompat {
    private static final ResourceLocation TRANSMUTATION_TABLE = new ResourceLocation("projecte", "transmutation_table");

    private static boolean failed;
    private static boolean init;
    private static Class<?> cIItemEmc;
    private static Class<?> cTransmutationTablet;
    private static Method mAddEmc;
    private static Method mGetStoredEmc;
    private static Method mGetMaximumEmc;
    private static Object emcProxy;
    private static Method mGetValue;
    private static Capability<?> knowledgeCap;
    private static Method mAddKnowledge;
    private static Method mGetEmc;
    private static Method mSetEmc;
    private static Method mSync;

    private ProjectECompat() {
    }

    private static boolean ensureInit() {
        if (failed) {
            return false;
        }
        if (init) {
            return true;
        }
        try {
            cIItemEmc = Class.forName("moze_intel.projecte.api.item.IItemEmc");
            mAddEmc = cIItemEmc.getMethod("addEmc", ItemStack.class, long.class);
            mGetStoredEmc = cIItemEmc.getMethod("getStoredEmc", ItemStack.class);
            mGetMaximumEmc = cIItemEmc.getMethod("getMaximumEmc", ItemStack.class);
            Class<?> cProjectEAPI = Class.forName("moze_intel.projecte.api.ProjectEAPI");
            emcProxy = cProjectEAPI.getMethod("getEMCProxy").invoke(null);
            Class<?> cIEMCProxy = Class.forName("moze_intel.projecte.api.proxy.IEMCProxy");
            mGetValue = cIEMCProxy.getMethod("getValue", ItemStack.class);
            Field capField = cProjectEAPI.getField("KNOWLEDGE_CAPABILITY");
            knowledgeCap = (Capability<?>) capField.get(null);
            Class<?> cKnowledge = Class.forName("moze_intel.projecte.api.capabilities.IKnowledgeProvider");
            mAddKnowledge = cKnowledge.getMethod("addKnowledge", ItemStack.class);
            mGetEmc = cKnowledge.getMethod("getEmc");
            mSetEmc = cKnowledge.getMethod("setEmc", long.class);
            mSync = cKnowledge.getMethod("sync", EntityPlayerMP.class);
            try {
                cTransmutationTablet = Class.forName("moze_intel.projecte.gameObjs.items.TransmutationTablet");
            } catch (Throwable ignored) {
            }
            init = true;
            return true;
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }

    public static boolean isTransmutationTable(IBlockState state) {
        return TRANSMUTATION_TABLE.equals(state.getBlock().getRegistryName());
    }

    public static boolean isTransmutationTablet(Item item) {
        if (!ensureInit() || item == null || cTransmutationTablet == null) {
            return false;
        }
        return cTransmutationTablet.isInstance(item);
    }

    public static boolean isEmcHolder(ItemStack stack) {
        if (!ensureInit() || stack.isEmpty()) {
            return false;
        }
        return cIItemEmc.isInstance(stack.getItem());
    }

    public static TransferService.LootSink emcSink(ItemStack holderStack) {
        if (!ensureInit()) {
            return null;
        }
        final Item item = holderStack.getItem();
        if (!cIItemEmc.isInstance(item)) {
            return null;
        }
        final ItemStack holder = holderStack;
        return new TransferService.LootSink() {
            @Override
            public ItemStack insert(ItemStack stack) {
                try {
                    long emcPerItem = (Long) mGetValue.invoke(emcProxy, stack);
                    if (emcPerItem <= 0) {
                        return stack;
                    }
                    long stored = (Long) mGetStoredEmc.invoke(item, holder);
                    long max = (Long) mGetMaximumEmc.invoke(item, holder);
                    long capacity = max - stored;
                    if (capacity <= 0) {
                        return stack;
                    }
                    int converted = (int) Math.min(stack.getCount(), capacity / emcPerItem);
                    if (converted <= 0) {
                        return stack;
                    }
                    mAddEmc.invoke(item, holder, (long) converted * emcPerItem);
                    if (converted >= stack.getCount()) {
                        return ItemStack.EMPTY;
                    }
                    ItemStack rem = stack.copy();
                    rem.setCount(stack.getCount() - converted);
                    return rem;
                } catch (Throwable t) {
                    return stack;
                }
            }
        };
    }

    public static TransferService.LootSink personalEmcSink(EntityPlayerMP player) {
        if (!ensureInit()) {
            return null;
        }
        final Object knowledge = getKnowledge(player);
        if (knowledge == null) {
            return null;
        }
        return new TransferService.LootSink() {
            @Override
            public ItemStack insert(ItemStack stack) {
                try {
                    long emcPerItem = (Long) mGetValue.invoke(emcProxy, stack);
                    if (emcPerItem <= 0) {
                        return stack;
                    }
                    mAddKnowledge.invoke(knowledge, stack);
                    long current = (Long) mGetEmc.invoke(knowledge);
                    mSetEmc.invoke(knowledge, current + emcPerItem * stack.getCount());
                    return ItemStack.EMPTY;
                } catch (Throwable t) {
                    return stack;
                }
            }
        };
    }

    public static void syncPersonal(EntityPlayerMP player) {
        if (!ensureInit()) {
            return;
        }
        Object knowledge = getKnowledge(player);
        if (knowledge != null) {
            try {
                mSync.invoke(knowledge, player);
            } catch (Throwable t) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Object getKnowledge(EntityPlayerMP player) {
        try {
            return player.getCapability((Capability<Object>) knowledgeCap, null);
        } catch (Throwable t) {
            return null;
        }
    }
}
