package com.cole.lootall;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

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
        if (GAMESTAGES) {
            lootAllRequiredStage = LOOT_ALL_STAGE.get();
            autoLootRequiredStage = AUTO_LOOT_STAGE.get();
            transferRequiredStage = TRANSFER_STAGE.get();
        }
    }
}
