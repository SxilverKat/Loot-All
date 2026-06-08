package com.cole.lootall.server;

import com.cole.lootall.Config;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class LootFilter {
    private static Set<ResourceLocation> skipItems = new HashSet<ResourceLocation>();
    private static Set<Integer> skipOreIds = new HashSet<Integer>();
    private static Set<String> skipMods = new HashSet<String>();
    private static Set<String> rarityNames = new HashSet<String>();

    private LootFilter() {
    }

    public static void rebuild(String[] skipList, String[] rarityList) {
        Set<ResourceLocation> items = new HashSet<ResourceLocation>();
        Set<Integer> ores = new HashSet<Integer>();
        Set<String> mods = new HashSet<String>();
        for (String raw : skipList) {
            if (raw == null) {
                continue;
            }
            String entry = raw.trim();
            if (entry.isEmpty()) {
                continue;
            }
            if (entry.startsWith("@")) {
                String namespace = entry.substring(1).trim().toLowerCase(Locale.ROOT);
                if (!namespace.isEmpty()) {
                    mods.add(namespace);
                }
            } else if (entry.startsWith("#")) {
                String ore = entry.substring(1).trim();
                if (!ore.isEmpty() && OreDictionary.doesOreNameExist(ore)) {
                    ores.add(OreDictionary.getOreID(ore));
                }
            } else {
                ResourceLocation rl = parse(entry);
                if (rl != null) {
                    items.add(rl);
                }
            }
        }

        Set<String> rarities = new HashSet<String>();
        for (String raw : rarityList) {
            if (raw == null) {
                continue;
            }
            String entry = raw.trim();
            if (!entry.isEmpty()) {
                rarities.add(entry.toUpperCase(Locale.ROOT));
            }
        }

        skipItems = items;
        skipOreIds = ores;
        skipMods = mods;
        rarityNames = rarities;
    }

    private static ResourceLocation parse(String entry) {
        try {
            return new ResourceLocation(entry);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean shouldSkip(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (!(skipItems.isEmpty() && skipOreIds.isEmpty() && skipMods.isEmpty())) {
            boolean listed = matchesList(stack, id);
            if (Config.skipListMode == Config.ListMode.WHITELIST) {
                if (!listed) {
                    return true;
                }
            } else if (listed) {
                return true;
            }
        }

        if (isGear(stack.getItem())) {
            if (Config.skipArmorAndTools) {
                return true;
            }
            if (Config.skipUnenchantedGear && !stack.isItemEnchanted()) {
                return true;
            }
        }

        if (Config.skipNonStackable && stack.getMaxStackSize() == 1) {
            return true;
        }

        Config.RarityMode mode = Config.rarityMode;
        if (mode != null && mode != Config.RarityMode.OFF && !rarityNames.isEmpty()) {
            boolean listed = rarityNames.contains(stack.getRarity().name().toUpperCase(Locale.ROOT));
            if (mode == Config.RarityMode.ONLY && !listed) {
                return true;
            }
            if (mode == Config.RarityMode.SKIP && listed) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesList(ItemStack stack, ResourceLocation id) {
        if (id != null) {
            if (skipMods.contains(id.getResourceDomain())) {
                return true;
            }
            if (skipItems.contains(id)) {
                return true;
            }
        }
        if (!skipOreIds.isEmpty()) {
            for (int oreId : OreDictionary.getOreIDs(stack)) {
                if (skipOreIds.contains(oreId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isGear(Item item) {
        return item instanceof ItemArmor
                || item instanceof ItemElytra
                || item instanceof ItemSword
                || item instanceof ItemTool
                || item instanceof ItemHoe
                || item instanceof ItemBow
                || item instanceof ItemShield
                || item instanceof ItemFishingRod
                || item instanceof ItemShears;
    }
}
