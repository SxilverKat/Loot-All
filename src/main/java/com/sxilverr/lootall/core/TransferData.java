package com.sxilverr.lootall.core;

import net.minecraft.core.BlockPos;
//? if >=1.21.1
/*import net.minecraft.core.HolderLookup;*/
//? if >=1.20 {
import net.minecraft.core.registries.Registries;
//?} else {
/*import net.minecraft.core.Registry;*/
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransferData extends SavedData {
    private static final String NAME = "lootall_transfer";

    public interface Target {
    }

    public static final class BlockTarget implements Target {
        private final ResourceKey<Level> dimension;
        private final BlockPos pos;

        public BlockTarget(ResourceKey<Level> dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }

        public ResourceKey<Level> dimension() {
            return dimension;
        }

        public BlockPos pos() {
            return pos;
        }
    }

    public static final class ItemTarget implements Target {
        private final ResourceLocation item;

        public ItemTarget(ResourceLocation item) {
            this.item = item;
        }

        public ResourceLocation item() {
            return item;
        }
    }

    private final Map<UUID, Target> targets = new HashMap<>();

    //? if <1.18 {
    /*public TransferData() {
        super(NAME);
    }
    *///?}

    public static TransferData get(MinecraftServer server) {
        //? if >=1.21.1 {
        /*return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TransferData::new, TransferData::load, null), NAME);
        *///?} else if >=1.18 {
        return server.overworld().getDataStorage().computeIfAbsent(
                TransferData::load, TransferData::new, NAME);
        //?} else {
        /*return server.overworld().getDataStorage().computeIfAbsent(
                TransferData::new, NAME);
        *///?}
    }

    public Target getTarget(UUID player) {
        return targets.get(player);
    }

    public void setBlockTarget(UUID player, ResourceKey<Level> dimension, BlockPos pos) {
        targets.put(player, new BlockTarget(dimension, pos));
        setDirty();
    }

    public void setItemTarget(UUID player, ResourceLocation item) {
        targets.put(player, new ItemTarget(item));
        setDirty();
    }

    public void clear(UUID player) {
        if (targets.remove(player) != null) {
            setDirty();
        }
    }

    private static ResourceLocation rl(String s) {
        //? if >=1.21.1 {
        /*return ResourceLocation.parse(s);
        *///?} else {
        return new ResourceLocation(s);
        //?}
    }

    private static ResourceKey<Level> dimKey(String dimension) {
        //? if >=1.20 {
        return ResourceKey.create(Registries.DIMENSION, rl(dimension));
        //?} else {
        /*return ResourceKey.create(Registry.DIMENSION_REGISTRY, rl(dimension));*/
        //?}
    }

    //? if >=1.18 {
    public static TransferData load(CompoundTag tag/*? if >=1.21.1 {*//*, HolderLookup.Provider registries*//*?}*/) {
        TransferData data = new TransferData();
        ListTag list = tag.getList("targets", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID player = entry.getUUID("player");
            if ("item".equals(entry.getString("type"))) {
                data.targets.put(player, new ItemTarget(rl(entry.getString("item"))));
            } else {
                ResourceKey<Level> dimension = dimKey(entry.getString("dimension"));
                BlockPos pos = new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
                data.targets.put(player, new BlockTarget(dimension, pos));
            }
        }
        return data;
    }
    //?} else {
    /*@Override
    public void load(CompoundTag tag) {
        targets.clear();
        ListTag list = tag.getList("targets", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID player = entry.getUUID("player");
            if ("item".equals(entry.getString("type"))) {
                targets.put(player, new ItemTarget(rl(entry.getString("item"))));
            } else {
                ResourceKey<Level> dimension = dimKey(entry.getString("dimension"));
                BlockPos pos = new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
                targets.put(player, new BlockTarget(dimension, pos));
            }
        }
    }
    *///?}

    @Override
    public CompoundTag save(CompoundTag tag/*? if >=1.21.1 {*//*, HolderLookup.Provider registries*//*?}*/) {
        ListTag list = new ListTag();
        targets.forEach((player, target) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", player);
            if (target instanceof ItemTarget) {
                ItemTarget item = (ItemTarget) target;
                entry.putString("type", "item");
                entry.putString("item", item.item().toString());
            } else if (target instanceof BlockTarget) {
                BlockTarget block = (BlockTarget) target;
                entry.putString("type", "block");
                entry.putString("dimension", block.dimension().location().toString());
                entry.putInt("x", block.pos().getX());
                entry.putInt("y", block.pos().getY());
                entry.putInt("z", block.pos().getZ());
            }
            list.add(entry);
        });
        tag.put("targets", list);
        return tag;
    }
}
