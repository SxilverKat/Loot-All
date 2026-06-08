package com.cole.lootall;

import com.cole.lootall.server.LootFilter;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import java.io.File;

public final class Config {
    public enum RarityMode { OFF, ONLY, SKIP }

    public enum ListMode { BLACKLIST, WHITELIST }

    private static final String CATEGORY_FILTERS = "item_filters";
    private static final String CATEGORY_STAGES = "game_stages";

    private static Configuration config;
    private static final boolean GAMESTAGES = Loader.isModLoaded("gamestages");

    public static int range = 20;
    public static boolean includeMinecarts = true;
    public static boolean feedbackMessage = true;
    public static boolean playSound = true;
    public static boolean excludeBlockedContainers = false;
    public static boolean autoLooting = false;
    public static int autoLootingTimer = 20;
    public static boolean enableLootingTransfer = true;
    public static int maxLootTransferDistance = 0;
    public static boolean transferRequireSameDimension = false;
    public static boolean transferRequireLoadedChunk = false;
    public static boolean skipArmorAndTools = false;
    public static boolean skipNonStackable = false;
    public static boolean skipUnenchantedGear = false;
    public static RarityMode rarityMode = RarityMode.OFF;
    public static ListMode skipListMode = ListMode.BLACKLIST;
    public static String lootAllRequiredStage = "";
    public static String autoLootRequiredStage = "";
    public static String transferRequiredStage = "";
    public static String[] skipList = new String[0];
    public static String[] rarityList = new String[0];

    private Config() {
    }

    public static void init(File file) {
        config = new Configuration(file);
        load();
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void load() {
        range = config.getInt("range", Configuration.CATEGORY_GENERAL, 20, 1, 5000,
                "Radius in blocks to search for loot containers");
        includeMinecarts = config.getBoolean("includeMinecarts", Configuration.CATEGORY_GENERAL, true,
                "Loot nearby minecarts that have a loot table");
        feedbackMessage = config.getBoolean("feedbackMessage", Configuration.CATEGORY_GENERAL, true,
                "Show an action bar message after looting");
        playSound = config.getBoolean("playSound", Configuration.CATEGORY_GENERAL, true,
                "Play a pickup sound after looting");
        excludeBlockedContainers = config.getBoolean("excludeBlockedContainers", Configuration.CATEGORY_GENERAL, false,
                "Exclude blocked containers: skip chests that cannot be opened (a block on top)");
        autoLooting = config.getBoolean("autoLooting", Configuration.CATEGORY_GENERAL, false,
                "Automatically loot nearby containers on a timer, without pressing the key");
        autoLootingTimer = config.getInt("autoLootingTimer", Configuration.CATEGORY_GENERAL, 20, 1, 3600,
                "How often auto looting runs, in seconds");
        enableLootingTransfer = config.getBoolean("enableLootingTransfer", Configuration.CATEGORY_GENERAL, true,
                "Enable the loot transfer system.");
        maxLootTransferDistance = config.getInt("maxLootTransferDistance", Configuration.CATEGORY_GENERAL, 0, 0, 100000,
                "Maximum distance in blocks to a transfer target; 0 = unlimited. Only applies when the target is in the same dimension as the player.");
        transferRequireSameDimension = config.getBoolean("transferRequireSameDimension", Configuration.CATEGORY_GENERAL, false,
                "Require the transfer target to be in the same dimension as the player");
        transferRequireLoadedChunk = config.getBoolean("transferRequireLoadedChunk", Configuration.CATEGORY_GENERAL, false,
                "Require the transfer target's chunk to already be loaded. If false, the chunk is briefly loaded to receive items.");

        config.setCategoryComment(CATEGORY_FILTERS,
                "Filter which items get looted. Skipped items are left in the container.");
        skipList = config.getStringList("skipList", CATEGORY_FILTERS, new String[0],
                "Items the skipList filter works from (see skipListMode). Formats: 'modid:item', '#oreDictName', '@modid'");
        String listMode = config.getString("skipListMode", CATEGORY_FILTERS, "BLACKLIST",
                "How the skipList is used:\n  BLACKLIST = loot everything EXCEPT what is listed.\n  WHITELIST = loot ONLY what is listed.\nAn empty skipList disables this filter in either mode.",
                new String[] {"BLACKLIST", "WHITELIST"});
        skipListMode = parseListMode(listMode);
        skipArmorAndTools = config.getBoolean("skipArmorAndTools", CATEGORY_FILTERS, false,
                "Skip all armor, tools, and weapons.");
        skipNonStackable = config.getBoolean("skipNonStackable", CATEGORY_FILTERS, false,
                "Skip all items that only stack to 1.");
        skipUnenchantedGear = config.getBoolean("skipUnenchantedGear", CATEGORY_FILTERS, false,
                "Skip armor, tools, and weapons UNLESS they are enchanted.");
        String mode = config.getString("rarityFilterMode", CATEGORY_FILTERS, "OFF",
                "Rarity filter mode:\n  OFF  = no rarity filtering.\n  ONLY = loot ONLY the tiers listed in rarityList.\n  SKIP = loot everything EXCEPT the tiers listed in rarityList.",
                new String[] {"OFF", "ONLY", "SKIP"});
        rarityMode = parseRarityMode(mode);
        rarityList = config.getStringList("rarityList", CATEGORY_FILTERS, new String[0],
                "Rarity tiers for the filter. Vanilla: COMMON, UNCOMMON, RARE, EPIC.\nNote: enchanting an item raises its rarity in vanilla.");

        if (GAMESTAGES) {
            config.setCategoryComment(CATEGORY_STAGES, "Restrict Loot All abilities behind Game Stages.");
            lootAllRequiredStage = config.getString("lootAllRequiredStage", CATEGORY_STAGES, "",
                    "Stage a player must have to use the Loot All key.");
            autoLootRequiredStage = config.getString("autoLootRequiredStage", CATEGORY_STAGES, "",
                    "Stage a player must have for auto-looting to run for them.");
            transferRequiredStage = config.getString("transferRequiredStage", CATEGORY_STAGES, "",
                    "Stage a player must have to use the Loot Transfer system.");
        }

        if (config.hasChanged()) {
            config.save();
        }
        LootFilter.rebuild(skipList, rarityList);
    }

    private static RarityMode parseRarityMode(String value) {
        try {
            return RarityMode.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return RarityMode.OFF;
        }
    }

    private static ListMode parseListMode(String value) {
        try {
            return ListMode.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ListMode.BLACKLIST;
        }
    }

    public static boolean transferEnabledForRegistration() {
        return enableLootingTransfer;
    }
}
