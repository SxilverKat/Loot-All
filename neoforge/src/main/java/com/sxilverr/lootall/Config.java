package com.sxilverr.lootall;

import com.sxilverr.lootall.config.LootConfig;
import com.sxilverr.lootall.core.LootFilter;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@EventBusSubscriber(modid = Config.MOD_ID)
public final class Config {
    public static final String MOD_ID = "lootall";

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue RANGE = BUILDER
            .comment("Radius in blocks to search for loot containers")
            .defineInRange("range", 20, 1, 5000);

    private static final ModConfigSpec.BooleanValue INCLUDE_MINECARTS = BUILDER
            .comment("Loot nearby minecarts that have a loot table")
            .define("includeMinecarts", true);

    private static final ModConfigSpec.BooleanValue FEEDBACK_MESSAGE = BUILDER
            .comment("Show an action bar message after looting")
            .define("feedbackMessage", true);

    private static final ModConfigSpec.BooleanValue PLAY_SOUND = BUILDER
            .comment("Play a pickup sound after looting")
            .define("playSound", true);

    private static final ModConfigSpec.BooleanValue EXCLUDE_BLOCKED = BUILDER
            .comment("Exclude blocked containers: skip chests that cannot be opened (A block on top)")
            .define("excludeBlockedContainers", false);

    private static final ModConfigSpec.BooleanValue AUTO_LOOTING = BUILDER
            .comment("Automatically loot nearby containers on a timer, without pressing the key")
            .define("autoLooting", false);

    private static final ModConfigSpec.IntValue AUTO_LOOTING_TIMER = BUILDER
            .comment("How often auto looting runs, in seconds")
            .defineInRange("autoLootingTimer", 20, 1, 3600);

    private static final ModConfigSpec.BooleanValue ENABLE_TRANSFER = BUILDER
            .comment("Enable the loot transfer system.")
            .define("enableLootingTransfer", true);

    private static final ModConfigSpec.IntValue MAX_TRANSFER_DISTANCE = BUILDER
            .comment("Maximum distance in blocks to a transfer target; 0 = unlimited. Only applies when the target is in the same dimension as the player.")
            .defineInRange("maxLootTransferDistance", 0, 0, 100000);

    private static final ModConfigSpec.BooleanValue TRANSFER_SAME_DIMENSION = BUILDER
            .comment("Require the transfer target to be in the same dimension as the player")
            .define("transferRequireSameDimension", false);

    private static final ModConfigSpec.BooleanValue TRANSFER_LOADED_CHUNK = BUILDER
            .comment("Require the transfer target's chunk to already be loaded. If false, the chunk is briefly loaded to receive items.")
            .define("transferRequireLoadedChunk", false);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> SKIP_LIST;
    private static final ModConfigSpec.EnumValue<LootConfig.ListMode> SKIP_LIST_MODE;
    private static final ModConfigSpec.BooleanValue SKIP_ARMOR_AND_TOOLS;
    private static final ModConfigSpec.BooleanValue SKIP_NON_STACKABLE;
    private static final ModConfigSpec.BooleanValue SKIP_UNENCHANTED_GEAR;
    private static final ModConfigSpec.EnumValue<LootConfig.RarityMode> RARITY_MODE;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> RARITY_LIST;

    static {
        BUILDER.comment("Filter which items get looted. Skipped items are left in the container.")
                .push("Item Filters");
        SKIP_LIST = BUILDER
                .comment("Items the skipList filter works from (see skipListMode).",
                        "Formats: 'modid:item', '#modid:tag', '@modid'")
                .defineListAllowEmpty("skipList", () -> List.of(), () -> "", Config::isValidListEntry);
        SKIP_LIST_MODE = BUILDER
                .comment("How the skipList is used:",
                        "  BLACKLIST = loot everything EXCEPT what is listed.",
                        "  WHITELIST = loot ONLY what is listed.",
                        "An empty skipList disables this filter in either mode.")
                .defineEnum("skipListMode", LootConfig.ListMode.BLACKLIST);
        SKIP_ARMOR_AND_TOOLS = BUILDER
                .comment("Skip all armor, tools, and weapons.")
                .define("skipArmorAndTools", false);
        SKIP_NON_STACKABLE = BUILDER
                .comment("Skip all items that only stack to 1.")
                .define("skipNonStackable", false);
        SKIP_UNENCHANTED_GEAR = BUILDER
                .comment("Skip armor, tools, and weapons UNLESS they are enchanted.")
                .define("skipUnenchantedGear", false);
        RARITY_MODE = BUILDER
                .comment("Rarity filter mode:",
                        "  OFF  = no rarity filtering.",
                        "  ONLY = loot ONLY the tiers listed in rarityList.",
                        "  SKIP = loot everything EXCEPT the tiers listed in rarityList.")
                .defineEnum("rarityFilterMode", LootConfig.RarityMode.OFF);
        RARITY_LIST = BUILDER
                .comment("Rarity tiers for the filter. Vanilla: COMMON, UNCOMMON, RARE, EPIC.",
                        "Note: enchanting an item raises its rarity in vanilla.")
                .defineListAllowEmpty("rarityList", () -> List.of(), () -> "", Config::isValidListEntry);
        BUILDER.pop();
    }

    private static boolean isValidListEntry(Object o) {
        return o instanceof String s && !s.isBlank();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean transferEnabledForRegistration() {
        return !SPEC.isLoaded() || ENABLE_TRANSFER.get();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        bake();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        bake();
    }

    private static void bake() {
        LootConfig.range = RANGE.get();
        LootConfig.includeMinecarts = INCLUDE_MINECARTS.get();
        LootConfig.feedbackMessage = FEEDBACK_MESSAGE.get();
        LootConfig.playSound = PLAY_SOUND.get();
        LootConfig.excludeBlockedContainers = EXCLUDE_BLOCKED.get();
        LootConfig.autoLooting = AUTO_LOOTING.get();
        LootConfig.autoLootingTimer = AUTO_LOOTING_TIMER.get();
        LootConfig.enableLootingTransfer = ENABLE_TRANSFER.get();
        LootConfig.maxLootTransferDistance = MAX_TRANSFER_DISTANCE.get();
        LootConfig.transferRequireSameDimension = TRANSFER_SAME_DIMENSION.get();
        LootConfig.transferRequireLoadedChunk = TRANSFER_LOADED_CHUNK.get();
        LootConfig.skipArmorAndTools = SKIP_ARMOR_AND_TOOLS.get();
        LootConfig.skipNonStackable = SKIP_NON_STACKABLE.get();
        LootConfig.skipUnenchantedGear = SKIP_UNENCHANTED_GEAR.get();
        LootConfig.rarityMode = RARITY_MODE.get();
        LootConfig.skipListMode = SKIP_LIST_MODE.get();
        LootFilter.rebuild(SKIP_LIST.get(), RARITY_LIST.get());
    }
}
