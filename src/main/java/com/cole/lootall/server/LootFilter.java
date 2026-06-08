package com.cole.lootall.server;

import com.cole.lootall.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LootFilter {
    private static Set<ResourceLocation> skipItems = Set.of();
    private static Set<TagKey<Item>> skipTags = Set.of();
    private static Set<String> skipMods = Set.of();
    private static Set<String> rarityNames = Set.of();

    private LootFilter() {
    }

    public static void rebuild(List<? extends String> skipList, List<? extends String> rarityList) {
        Set<ResourceLocation> items = new HashSet<>();
        Set<TagKey<Item>> tags = new HashSet<>();
        Set<String> mods = new HashSet<>();
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
                ResourceLocation rl = ResourceLocation.tryParse(entry.substring(1).trim());
                if (rl != null) {
                    tags.add(TagKey.create(Registries.ITEM, rl));
                }
            } else {
                ResourceLocation rl = ResourceLocation.tryParse(entry);
                if (rl != null) {
                    items.add(rl);
                }
            }
        }

        Set<String> rarities = new HashSet<>();
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
        skipTags = tags;
        skipMods = mods;
        rarityNames = rarities;
    }

    public static boolean shouldSkip(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (!(skipItems.isEmpty() && skipTags.isEmpty() && skipMods.isEmpty())) {
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
            if (Config.skipUnenchantedGear && !stack.isEnchanted()) {
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
            if (skipMods.contains(id.getNamespace())) {
                return true;
            }
            if (skipItems.contains(id)) {
                return true;
            }
        }
        for (TagKey<Item> tag : skipTags) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGear(Item item) {
        return item instanceof ArmorItem
                || item instanceof ElytraItem
                || item instanceof TieredItem
                || item instanceof ShieldItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem
                || item instanceof FishingRodItem
                || item instanceof ShearsItem;
    }
}
