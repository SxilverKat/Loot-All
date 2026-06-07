package com.cole.lootall.server;

import com.cole.lootall.Config;
import com.cole.lootall.network.LootFeedbackMessage;
import com.cole.lootall.network.PacketHandler;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public final class LootAllHandler {
    private static TransferService.ResolvedSink activeSink;
    private static int transferredCount;

    private LootAllHandler() {
    }

    public static void lootAll(EntityPlayerMP player) {
        lootAll(player, false);
    }

    public static void lootAll(EntityPlayerMP player, boolean auto) {
        WorldServer level = (WorldServer) player.world;
        int range = Config.range;
        double rangeSq = (double) range * range;

        TransferService.ResolvedSink sink = TransferService.resolveSink(player);
        activeSink = sink;
        transferredCount = 0;
        Result result = new Result();
        try {
            lootBlocks(player, level, range, rangeSq, result);
            if (Config.includeMinecarts) {
                lootMinecarts(player, level, range, rangeSq, result);
            }
        } finally {
            activeSink = null;
        }
        if (sink != null && transferredCount > 0 && sink.onComplete != null) {
            sink.onComplete.run();
        }
        ITextComponent transferName = (sink != null && transferredCount > 0) ? sink.name : null;
        sendFeedback(player, result, auto, transferName);
    }

    private static void lootBlocks(EntityPlayerMP player, WorldServer level, int range, double rangeSq, Result result) {
        BlockPos center = player.getPosition();
        int minChunkX = (center.getX() - range) >> 4;
        int maxChunkX = (center.getX() + range) >> 4;
        int minChunkZ = (center.getZ() - range) >> 4;
        int maxChunkZ = (center.getZ() + range) >> 4;

        List<TileEntity> targets = new ArrayList<TileEntity>();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                Chunk chunk = level.getChunkProvider().getLoadedChunk(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (TileEntity be : chunk.getTileEntityMap().values()) {
                    if (center.distanceSq(be.getPos()) <= rangeSq) {
                        targets.add(be);
                    }
                }
            }
        }

        for (TileEntity be : targets) {
            if (Config.excludeBlockedContainers && isBlockedChest(level, be.getPos())) {
                continue;
            }
            if (be instanceof TileEntityLockableLoot) {
                TileEntityLockableLoot loot = (TileEntityLockableLoot) be;
                if (loot.getLootTable() != null) {
                    loot.fillWithLoot(player);
                    result.items += drain(player, loot);
                    loot.markDirty();
                    result.containers++;
                }
            }
        }
    }

    private static void lootMinecarts(EntityPlayerMP player, WorldServer level, int range, double rangeSq, Result result) {
        AxisAlignedBB box = player.getEntityBoundingBox().grow(range);
        List<EntityMinecartContainer> carts = level.getEntitiesWithinAABB(EntityMinecartContainer.class, box);
        for (EntityMinecartContainer cart : carts) {
            if (player.getDistanceSq(cart) > rangeSq) {
                continue;
            }
            if (cart.getLootTable() != null) {
                cart.addLoot(player);
                result.items += drain(player, cart);
                cart.markDirty();
                result.containers++;
            }
        }
    }

    public static int drain(EntityPlayerMP player, IInventory container) {
        int moved = 0;
        for (int i = 0; i < container.getSizeInventory(); i++) {
            ItemStack stack = container.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (LootFilter.shouldSkip(stack)) {
                    continue;
                }
                moved += stack.getCount();
                giveOrDrop(player, stack);
                container.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }
        return moved;
    }

    public static void giveOrDrop(EntityPlayerMP player, ItemStack stack) {
        if (activeSink != null) {
            int before = stack.getCount();
            ItemStack remaining = activeSink.sink.insert(stack);
            transferredCount += before - remaining.getCount();
            if (!remaining.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, remaining);
            }
        } else {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
    }

    private static boolean isBlockedChest(WorldServer level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof BlockChest
                && level.getBlockState(pos.up()).isNormalCube();
    }

    private static void sendFeedback(EntityPlayerMP player, Result result, boolean auto, ITextComponent transferName) {
        if (Config.feedbackMessage && !(auto && result.containers == 0)) {
            ITextComponent message;
            if (result.containers == 0) {
                message = new TextComponentTranslation("message.lootall.nothing");
            } else {
                ITextComponent itemWord = new TextComponentTranslation(
                        result.items == 1 ? "message.lootall.item" : "message.lootall.items");
                ITextComponent containerWord = new TextComponentTranslation(
                        result.containers == 1 ? "message.lootall.container" : "message.lootall.containers");
                message = new TextComponentTranslation("message.lootall.looted",
                        result.items, itemWord, result.containers, containerWord);
            }
            PacketHandler.INSTANCE.sendTo(new LootFeedbackMessage(message, transferName), player);
        }
        if (Config.playSound && result.containers > 0) {
            player.connection.sendPacket(new SPacketSoundEffect(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS,
                    player.posX, player.posY, player.posZ, 1.0F, 1.0F));
        }
    }

    private static final class Result {
        private int items;
        private int containers;
    }
}
