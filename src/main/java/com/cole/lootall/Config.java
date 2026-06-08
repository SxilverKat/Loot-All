package com.cole.lootall;

import com.cole.lootall.server.LootFilter;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = LootAll.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final boolean GAMESTAGES = ModList.get().isLoaded("gamestages");

    private static final ForgeConfigSpec.IntValue RANGE = BUILDER
            .comment("Radius in blocks to search for loot containers")
            .defineInRange("range", 20, 1, 5000);

    private static final ForgeConfigSpec.BooleanValue INCLUDE_MINECARTS = BUILDER
            .comment("Loot nearby minecarts that have a loot table")
            .define("includeMinecarts", true);

    private static final ForgeConfigSpec.BooleanValue FEEDBACK_MESSAGE = BUILDER
            .comment("Show an action bar message after looting")
            .define("feedbackMessage", true);

    private static final ForgeConfigSpec.BooleanValue PLAY_SOUND = BUILDER
            .comment("Play a pickup sound after looting")
            .define("playSound", true);

    private static final ForgeConfigSpec.BooleanValue EXCLUDE_BLOCKED = BUILDER
            .comment("Exclude blocked containers: skip chests that cannot be opened (A block on top)")
            .define("excludeBlockedContainers", false);

    private static final ForgeConfigSpec.BooleanValue AUTO_LOOTING = BUILDER
            .comment("Automatically loot nearby containers on a timer, without pressing the key")
            .define("autoLooting", false);

    private static final ForgeConfigSpec.IntValue AUTO_LOOTING_TIMER = BUILDER
            .comment("How often auto looting runs, in seconds")
            .defineInRange("autoLootingTimer", 20, 1, 3600);

    private static final ForgeConfigSpec.BooleanValue ENABLE_TRANSFER = BUILDER
            .comment("Enable the loot transfer system.")
            .define("enableLootingTransfer", true);

    private static final ForgeConfigSpec.IntValue MAX_TRANSFER_DISTANCE = BUILDER
            .comment("Maximum distance in blocks to a transfer target; 0 = unlimited. Only applies when the target is in the same dimension as the player.")
            .defineInRange("maxLootTransferDistance", 0, 0, 100000);

    private static final ForgeConfigSpec.BooleanValue TRANSFER_SAME_DIMENSION = BUILDER
            .comment("Require the transfer target to be in the same dimension as the player")
            .define("transferRequireSameDimension", false);

    private static final ForgeConfigSpec.BooleanValue TRANSFER_LOADED_CHUNK = BUILDER
            .comment("Require the transfer target's chunk to already be loaded. If false, the chunk is briefly loaded to receive items.")
            .define("transferRequireLoadedChunk", false);

    public enum RarityMode { OFF, ONLY, SKIP }

    public enum ListMode { BLACKLIST, WHITELIST }

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SKIP_LIST;
    private static final ForgeConfigSpec.EnumValue<ListMode> SKIP_LIST_MODE;
    private static final ForgeConfigSpec.BooleanValue SKIP_ARMOR_AND_TOOLS;
    private static final ForgeConfigSpec.BooleanValue SKIP_NON_STACKABLE;
    private static final ForgeConfigSpec.BooleanValue SKIP_UNENCHANTED_GEAR;
    private static final ForgeConfigSpec.EnumValue<RarityMode> RARITY_MODE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> RARITY_LIST;

    static {
        BUILDER.comment("Filter which items get looted. Skipped items are left in the container.")
                .push("Item Filters");
        SKIP_LIST = BUILDER
                .comment("Items the skipList filter works from (see skipListMode).",
                        "Formats: 'modid:item', '#modid:tag', '@modid'")
                .defineListAllowEmpty(List.of("skipList"), () -> List.of(), Config::isValidListEntry);
        SKIP_LIST_MODE = BUILDER
                .comment("How the skipList is used:",
                        "  BLACKLIST = loot everything EXCEPT what is listed.",
                        "  WHITELIST = loot ONLY what is listed.",
                        "An empty skipList disables this filter in either mode.")
                .defineEnum("skipListMode", ListMode.BLACKLIST);
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
                .defineEnum("rarityFilterMode", RarityMode.OFF);
        RARITY_LIST = BUILDER
                .comment("Rarity tiers for the filter. Vanilla: COMMON, UNCOMMON, RARE, EPIC.",
                        "Note: enchanting an item raises its rarity in vanilla.")
                .defineListAllowEmpty(List.of("rarityList"), () -> List.of(), Config::isValidListEntry);
        BUILDER.pop();
    }

    private static boolean isValidListEntry(Object o) {
        return o instanceof String s && !s.isBlank();
    }

    // Defined only when Game Stages is installed, grouped under a "Game Stages" category.
    private static final ForgeConfigSpec.ConfigValue<String> LOOT_ALL_STAGE;
    private static final ForgeConfigSpec.ConfigValue<String> AUTO_LOOT_STAGE;
    private static final ForgeConfigSpec.ConfigValue<String> TRANSFER_STAGE;

    static {
        if (GAMESTAGES) {
            BUILDER.comment("Restrict Loot All abilities behind Game Stages.")
                    .push("Game Stages");
            LOOT_ALL_STAGE = BUILDER
                    .comment("Stage a player must have to use the Loot All key.")
                    .define("lootAllRequiredStage", "");
            AUTO_LOOT_STAGE = BUILDER
                    .comment("Stage a player must have for auto-looting to run for them.")
                    .define("autoLootRequiredStage", "");
            TRANSFER_STAGE = BUILDER
                    .comment("Stage a player must have to use the Loot Transfer system.")
                    .define("transferRequiredStage", "");
            BUILDER.pop();
        } else {
            LOOT_ALL_STAGE = null;
            AUTO_LOOT_STAGE = null;
            TRANSFER_STAGE = null;
        }
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int range;
    public static boolean includeMinecarts;
    public static boolean feedbackMessage;
    public static boolean playSound;
    public static boolean excludeBlockedContainers;
    public static boolean autoLooting;
    public static int autoLootingTimer;
    public static boolean enableLootingTransfer;
    public static int maxLootTransferDistance;
    public static boolean transferRequireSameDimension;
    public static boolean transferRequireLoadedChunk;
    public static boolean skipArmorAndTools;
    public static boolean skipNonStackable;
    public static boolean skipUnenchantedGear;
    public static RarityMode rarityMode = RarityMode.OFF;
    public static ListMode skipListMode = ListMode.BLACKLIST;
    public static String lootAllRequiredStage;
    public static String autoLootRequiredStage;
    public static String transferRequiredStage;

    public static boolean transferEnabledForRegistration() {
        return !SPEC.isLoaded() || ENABLE_TRANSFER.get();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        range = RANGE.get();
        includeMinecarts = INCLUDE_MINECARTS.get();
        feedbackMessage = FEEDBACK_MESSAGE.get();
        playSound = PLAY_SOUND.get();
        excludeBlockedContainers = EXCLUDE_BLOCKED.get();
        autoLooting = AUTO_LOOTING.get();
        autoLootingTimer = AUTO_LOOTING_TIMER.get();
        enableLootingTransfer = ENABLE_TRANSFER.get();
        maxLootTransferDistance = MAX_TRANSFER_DISTANCE.get();
        transferRequireSameDimension = TRANSFER_SAME_DIMENSION.get();
        transferRequireLoadedChunk = TRANSFER_LOADED_CHUNK.get();
        skipArmorAndTools = SKIP_ARMOR_AND_TOOLS.get();
        skipNonStackable = SKIP_NON_STACKABLE.get();
        skipUnenchantedGear = SKIP_UNENCHANTED_GEAR.get();
        rarityMode = RARITY_MODE.get();
        skipListMode = SKIP_LIST_MODE.get();
        LootFilter.rebuild(SKIP_LIST.get(), RARITY_LIST.get());
        if (GAMESTAGES) {
            lootAllRequiredStage = LOOT_ALL_STAGE.get();
            autoLootRequiredStage = AUTO_LOOT_STAGE.get();
            transferRequiredStage = TRANSFER_STAGE.get();
        }
    }
}
