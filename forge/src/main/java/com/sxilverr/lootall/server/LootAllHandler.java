package com.sxilverr.lootall.server;
import com.sxilverr.lootall.core.LootFilter;

import com.sxilverr.lootall.Compat;
import com.sxilverr.lootall.Text;
import com.sxilverr.lootall.config.LootConfig;

import com.sxilverr.lootall.compat.LootrCompat;
import com.sxilverr.lootall.network.LootAllNetwork;
import com.sxilverr.lootall.network.LootFeedbackPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemHandlerHelper;
//? if >=1.17 {
import net.minecraftforge.network.PacketDistributor;
//?} else {
/*import net.minecraftforge.fml.network.PacketDistributor;*/
//?}

import java.util.ArrayList;
import java.util.List;

public class LootAllHandler {
    private static final boolean LOOTR = ModList.get().isLoaded("lootr");

    private static TransferService.ResolvedSink activeSink;
    private static int transferredCount;

    public static void lootAll(ServerPlayer player) {
        lootAll(player, false);
    }

    public static void lootAll(ServerPlayer player, boolean auto) {
        ServerLevel level = (ServerLevel) Compat.level(player);
        int range = LootConfig.range;
        double rangeSq = (double) range * range;

        TransferService.ResolvedSink sink = TransferService.resolveSink(player);
        activeSink = sink;
        transferredCount = 0;
        Result result = new Result();
        try {
            lootBlocks(player, level, range, rangeSq, result);
            if (LootConfig.includeMinecarts) {
                lootMinecarts(player, level, range, rangeSq, result);
            }
        } finally {
            activeSink = null;
        }
        if (sink != null && transferredCount > 0 && sink.onComplete() != null) {
            sink.onComplete().run();
        }
        Component transferName = (sink != null && transferredCount > 0) ? sink.name() : null;
        sendFeedback(player, result, auto, transferName);
    }

    private static void lootBlocks(ServerPlayer player, ServerLevel level, int range, double rangeSq, Result result) {
        BlockPos center = player.blockPosition();
        int minChunkX = (center.getX() - range) >> 4;
        int maxChunkX = (center.getX() + range) >> 4;
        int minChunkZ = (center.getZ() - range) >> 4;
        int maxChunkZ = (center.getZ() + range) >> 4;

        List<BlockEntity> targets = new ArrayList<>();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (center.distSqr(be.getBlockPos()) <= rangeSq) {
                        targets.add(be);
                    }
                }
            }
        }

        for (BlockEntity be : targets) {
            if (LootConfig.excludeBlockedContainers && isBlockedChest(level, be.getBlockPos())) {
                continue;
            }
            if (LOOTR && LootrCompat.isLootrContainer(be)) {
                int looted = LootrCompat.lootContainer(player, be);
                if (looted >= 0) {
                    result.items += looted;
                    result.containers++;
                }
            } else if (be instanceof RandomizableContainerBlockEntity
                    && ((RandomizableContainerBlockEntity) be).lootTable != null) {
                RandomizableContainerBlockEntity rc = (RandomizableContainerBlockEntity) be;
                rc.unpackLootTable(player);
                result.items += drain(player, rc);
                rc.setChanged();
                result.containers++;
            }
        }
    }

    private static void lootMinecarts(ServerPlayer player, ServerLevel level, int range, double rangeSq, Result result) {
        AABB box = player.getBoundingBox().inflate(range);
        List<AbstractMinecartContainer> carts = level.getEntitiesOfClass(AbstractMinecartContainer.class, box);
        for (AbstractMinecartContainer cart : carts) {
            if (player.distanceToSqr(cart) > rangeSq) {
                continue;
            }
            if (LOOTR && LootrCompat.isLootrCart(cart)) {
                int looted = LootrCompat.lootCart(player, cart);
                if (looted >= 0) {
                    result.items += looted;
                    result.containers++;
                }
            //? if >=1.19 {
            } else if (cart.getLootTable() != null) {
                cart.unpackChestVehicleLootTable(player);
                result.items += drain(player, cart);
                cart.setChanged();
                result.containers++;
            }
            //?} else {
            /*} else if (cart.lootTable != null) {
                cart.unpackLootTable(player);
                result.items += drain(player, cart);
                cart.setChanged();
                result.containers++;
            }*/
            //?}
        }
    }

    public static int drain(ServerPlayer player, Container container) {
        int moved = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (LootFilter.shouldSkip(stack)) {
                    continue;
                }
                moved += stack.getCount();
                giveOrDrop(player, stack);
                container.setItem(i, ItemStack.EMPTY);
            }
        }
        return moved;
    }

    public static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (activeSink != null) {
            int before = stack.getCount();
            ItemStack remaining = activeSink.sink().insert(stack);
            transferredCount += before - remaining.getCount();
            if (!remaining.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, remaining);
            }
        } else {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
    }

    private static boolean isBlockedChest(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof ChestBlock
                && ChestBlock.isChestBlockedAt(level, pos);
    }

    private static void sendFeedback(ServerPlayer player, Result result, boolean auto, Component transferName) {
        if (LootConfig.feedbackMessage && !(auto && result.containers == 0)) {
            Component message;
            if (result.containers == 0) {
                message = Text.translatable("message.lootall.nothing");
            } else {
                Component itemWord = Text.translatable(
                        result.items == 1 ? "message.lootall.item" : "message.lootall.items");
                Component containerWord = Text.translatable(
                        result.containers == 1 ? "message.lootall.container" : "message.lootall.containers");
                message = Text.translatable("message.lootall.looted",
                        result.items, itemWord, result.containers, containerWord);
            }
            LootAllNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new LootFeedbackPacket(message, transferName));
        }
        if (LootConfig.playSound && result.containers > 0) {
            player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static final class Result {
        private int items;
        private int containers;
    }
}
