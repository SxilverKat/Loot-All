package com.sxilverr.lootall.client;

import com.sxilverr.lootall.Config;
import com.sxilverr.lootall.config.LootConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ClothConfigScreen {

    private ClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        Config.Data data = Config.data();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Loot All"))
                .setSavingRunnable(Config::persist);
        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        general.addEntry(eb.startIntField(Component.literal("Search range (blocks)"), data.range)
                .setMin(1).setMax(5000).setDefaultValue(20)
                .setSaveConsumer(v -> data.range = v).build());
        general.addEntry(eb.startBooleanToggle(Component.literal("Loot minecarts"), data.includeMinecarts)
                .setDefaultValue(true).setSaveConsumer(v -> data.includeMinecarts = v).build());
        general.addEntry(eb.startBooleanToggle(Component.literal("Skip blocked containers"), data.excludeBlockedContainers)
                .setDefaultValue(false).setSaveConsumer(v -> data.excludeBlockedContainers = v).build());
        general.addEntry(eb.startBooleanToggle(Component.literal("Show feedback message"), data.feedbackMessage)
                .setDefaultValue(true).setSaveConsumer(v -> data.feedbackMessage = v).build());
        general.addEntry(eb.startBooleanToggle(Component.literal("Play pickup sound"), data.playSound)
                .setDefaultValue(true).setSaveConsumer(v -> data.playSound = v).build());
        general.addEntry(eb.startBooleanToggle(Component.literal("Auto looting"), data.autoLooting)
                .setDefaultValue(false).setSaveConsumer(v -> data.autoLooting = v).build());
        general.addEntry(eb.startIntField(Component.literal("Auto looting interval (seconds)"), data.autoLootingTimer)
                .setMin(1).setMax(3600).setDefaultValue(20)
                .setSaveConsumer(v -> data.autoLootingTimer = v).build());

        ConfigCategory transfer = builder.getOrCreateCategory(Component.literal("Loot Transfer"));
        transfer.addEntry(eb.startBooleanToggle(Component.literal("Enable loot transfer"), data.enableLootingTransfer)
                .setDefaultValue(true).setSaveConsumer(v -> data.enableLootingTransfer = v).build());
        transfer.addEntry(eb.startIntField(Component.literal("Max transfer distance (0 = unlimited)"), data.maxLootTransferDistance)
                .setMin(0).setMax(100000).setDefaultValue(0)
                .setSaveConsumer(v -> data.maxLootTransferDistance = v).build());
        transfer.addEntry(eb.startBooleanToggle(Component.literal("Require same dimension"), data.transferRequireSameDimension)
                .setDefaultValue(false).setSaveConsumer(v -> data.transferRequireSameDimension = v).build());
        transfer.addEntry(eb.startBooleanToggle(Component.literal("Require loaded chunk"), data.transferRequireLoadedChunk)
                .setDefaultValue(false).setSaveConsumer(v -> data.transferRequireLoadedChunk = v).build());

        ConfigCategory filters = builder.getOrCreateCategory(Component.literal("Item Filters"));
        filters.addEntry(eb.startStrList(Component.literal("Skip list"), data.skipList)
                .setDefaultValue(List.of()).setSaveConsumer(v -> data.skipList = v).build());
        filters.addEntry(eb.startEnumSelector(Component.literal("Skip list mode"), LootConfig.ListMode.class,
                        Config.parseListMode(data.skipListMode))
                .setDefaultValue(LootConfig.ListMode.BLACKLIST)
                .setSaveConsumer(v -> data.skipListMode = v.name()).build());
        filters.addEntry(eb.startBooleanToggle(Component.literal("Skip armor, tools and weapons"), data.skipArmorAndTools)
                .setDefaultValue(false).setSaveConsumer(v -> data.skipArmorAndTools = v).build());
        filters.addEntry(eb.startBooleanToggle(Component.literal("Skip non-stackable items"), data.skipNonStackable)
                .setDefaultValue(false).setSaveConsumer(v -> data.skipNonStackable = v).build());
        filters.addEntry(eb.startBooleanToggle(Component.literal("Skip unenchanted gear"), data.skipUnenchantedGear)
                .setDefaultValue(false).setSaveConsumer(v -> data.skipUnenchantedGear = v).build());
        filters.addEntry(eb.startEnumSelector(Component.literal("Rarity filter mode"), LootConfig.RarityMode.class,
                        Config.parseRarityMode(data.rarityFilterMode))
                .setDefaultValue(LootConfig.RarityMode.OFF)
                .setSaveConsumer(v -> data.rarityFilterMode = v.name()).build());
        filters.addEntry(eb.startStrList(Component.literal("Rarity list"), data.rarityList)
                .setDefaultValue(List.of()).setSaveConsumer(v -> data.rarityList = v).build());

        return builder.build();
    }
}
