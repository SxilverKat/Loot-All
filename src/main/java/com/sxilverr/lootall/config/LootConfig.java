package com.sxilverr.lootall.config;

public final class LootConfig {

    public enum RarityMode { OFF, ONLY, SKIP }

    public enum ListMode { BLACKLIST, WHITELIST }

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

    private LootConfig() {
    }
}
