package com.sxilverr.lootall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sxilverr.lootall.config.LootConfig;
import com.sxilverr.lootall.core.LootFilter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Config {
    public static final String MOD_ID = "lootall";

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("lootall.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static Data data = new Data();

    private Config() {
    }

    public static final class Data {
        public int range = 20;
        public boolean includeMinecarts = true;
        public boolean feedbackMessage = true;
        public boolean playSound = true;
        public boolean excludeBlockedContainers = false;
        public boolean autoLooting = false;
        public int autoLootingTimer = 20;
        public boolean enableLootingTransfer = true;
        public int maxLootTransferDistance = 0;
        public boolean transferRequireSameDimension = false;
        public boolean transferRequireLoadedChunk = false;
        public List<String> skipList = new ArrayList<>();
        public String skipListMode = "BLACKLIST";
        public boolean skipArmorAndTools = false;
        public boolean skipNonStackable = false;
        public boolean skipUnenchantedGear = false;
        public String rarityFilterMode = "OFF";
        public List<String> rarityList = new ArrayList<>();
    }

    public static Data data() {
        return data;
    }

    public static boolean transferEnabledForRegistration() {
        return data.enableLootingTransfer;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    Data loaded = GSON.fromJson(reader, Data.class);
                    if (loaded != null) {
                        data = loaded;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        save();
        bake();
    }

    public static void persist() {
        save();
        bake();
    }

    private static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception ignored) {
        }
    }

    private static void bake() {
        LootConfig.range = clamp(data.range, 1, 5000);
        LootConfig.includeMinecarts = data.includeMinecarts;
        LootConfig.feedbackMessage = data.feedbackMessage;
        LootConfig.playSound = data.playSound;
        LootConfig.excludeBlockedContainers = data.excludeBlockedContainers;
        LootConfig.autoLooting = data.autoLooting;
        LootConfig.autoLootingTimer = clamp(data.autoLootingTimer, 1, 3600);
        LootConfig.enableLootingTransfer = data.enableLootingTransfer;
        LootConfig.maxLootTransferDistance = clamp(data.maxLootTransferDistance, 0, 100000);
        LootConfig.transferRequireSameDimension = data.transferRequireSameDimension;
        LootConfig.transferRequireLoadedChunk = data.transferRequireLoadedChunk;
        LootConfig.skipArmorAndTools = data.skipArmorAndTools;
        LootConfig.skipNonStackable = data.skipNonStackable;
        LootConfig.skipUnenchantedGear = data.skipUnenchantedGear;
        LootConfig.skipListMode = parseListMode(data.skipListMode);
        LootConfig.rarityMode = parseRarityMode(data.rarityFilterMode);
        LootFilter.rebuild(
                data.skipList != null ? data.skipList : List.of(),
                data.rarityList != null ? data.rarityList : List.of());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static LootConfig.ListMode parseListMode(String raw) {
        try {
            return LootConfig.ListMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return LootConfig.ListMode.BLACKLIST;
        }
    }

    public static LootConfig.RarityMode parseRarityMode(String raw) {
        try {
            return LootConfig.RarityMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return LootConfig.RarityMode.OFF;
        }
    }
}
