package com.sxilverr.lootall.core;

import net.minecraft.core.BlockPos;
//? if >=1.21.1
/*import net.minecraft.core.HolderLookup;*/
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

    public record BlockTarget(ResourceKey<Level> dimension, BlockPos pos) implements Target {
    }

    public record ItemTarget(ResourceLocation item) implements Target {
    }

    private final Map<UUID, Target> targets = new HashMap<>();

    public static TransferData get(MinecraftServer server) {
        //? if >=1.21.1 {
        /*return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TransferData::new, TransferData::load, null), NAME);
        *///?} else {
        return server.overworld().getDataStorage().computeIfAbsent(
                TransferData::load, TransferData::new, NAME);
        //?}
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
        return new ResourceLocation(s);//?}
    }

    public static TransferData load(CompoundTag tag/*? if >=1.21.1 {*//*, HolderLookup.Provider registries*//*?}*/) {
        TransferData data = new TransferData();
        ListTag list = tag.getList("targets", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID player = entry.getUUID("player");
            if ("item".equals(entry.getString("type"))) {
                data.targets.put(player, new ItemTarget(rl(entry.getString("item"))));
            } else {
                ResourceKey<Level> dimension = ResourceKey.create(
                        Registries.DIMENSION, rl(entry.getString("dimension")));
                BlockPos pos = new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
                data.targets.put(player, new BlockTarget(dimension, pos));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag/*? if >=1.21.1 {*//*, HolderLookup.Provider registries*//*?}*/) {
        ListTag list = new ListTag();
        targets.forEach((player, target) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", player);
            if (target instanceof ItemTarget item) {
                entry.putString("type", "item");
                entry.putString("item", item.item().toString());
            } else if (target instanceof BlockTarget block) {
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
