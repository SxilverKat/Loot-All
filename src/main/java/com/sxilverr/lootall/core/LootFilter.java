package com.sxilverr.lootall.core;

import com.sxilverr.lootall.config.LootConfig;
//? if >=1.20 {
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
//?} else {
/*import net.minecraft.core.Registry;*/
//?}
import net.minecraft.resources.ResourceLocation;
//? if >=1.18 {
import net.minecraft.tags.TagKey;
//?} else {
/*import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;*/
//?}
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LootFilter {
    private static Set<ResourceLocation> skipItems = Collections.emptySet();
    private static Set<ResourceLocation> skipTags = Collections.emptySet();
    private static Set<String> skipMods = Collections.emptySet();
    private static Set<String> rarityNames = Collections.emptySet();

    private LootFilter() {
    }

    public static void rebuild(List<? extends String> skipList, List<? extends String> rarityList) {
        Set<ResourceLocation> items = new HashSet<>();
        Set<ResourceLocation> tags = new HashSet<>();
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
                    tags.add(rl);
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

        //? if >=1.20 {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        //?} else {
        /*ResourceLocation id = Registry.ITEM.getKey(stack.getItem());*/
        //?}
        if (!(skipItems.isEmpty() && skipTags.isEmpty() && skipMods.isEmpty())) {
            boolean listed = matchesList(stack, id);
            if (LootConfig.skipListMode == LootConfig.ListMode.WHITELIST) {
                if (!listed) {
                    return true;
                }
            } else if (listed) {
                return true;
            }
        }

        if (isGear(stack.getItem())) {
            if (LootConfig.skipArmorAndTools) {
                return true;
            }
            if (LootConfig.skipUnenchantedGear && !stack.isEnchanted()) {
                return true;
            }
        }

        if (LootConfig.skipNonStackable && stack.getMaxStackSize() == 1) {
            return true;
        }

        LootConfig.RarityMode mode = LootConfig.rarityMode;
        if (mode != null && mode != LootConfig.RarityMode.OFF && !rarityNames.isEmpty()) {
            boolean listed = rarityNames.contains(stack.getRarity().name().toUpperCase(Locale.ROOT));
            if (mode == LootConfig.RarityMode.ONLY && !listed) {
                return true;
            }
            if (mode == LootConfig.RarityMode.SKIP && listed) {
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
        for (ResourceLocation tagId : skipTags) {
            //? if >=1.20 {
            if (stack.is(TagKey.create(Registries.ITEM, tagId))) {
                return true;
            }
            //?} else if >=1.18 {
            /*if (stack.is(TagKey.create(Registry.ITEM_REGISTRY, tagId))) {
                return true;
            }
            *///?} else {
            /*Tag<Item> tag = SerializationTags.getInstance().getItems().getTagOrEmpty(tagId);
            if (tag.contains(stack.getItem())) {
                return true;
            }
            *///?}
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
