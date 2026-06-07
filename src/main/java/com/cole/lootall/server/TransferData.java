package com.cole.lootall.server;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransferData extends WorldSavedData {
    private static final String NAME = "lootall_transfer";

    public static final class Target {
        public final boolean isItem;
        public final int dimension;
        public final BlockPos pos;
        public final ResourceLocation item;

        private Target(boolean isItem, int dimension, BlockPos pos, ResourceLocation item) {
            this.isItem = isItem;
            this.dimension = dimension;
            this.pos = pos;
            this.item = item;
        }

        public static Target block(int dimension, BlockPos pos) {
            return new Target(false, dimension, pos, null);
        }

        public static Target item(ResourceLocation item) {
            return new Target(true, 0, null, item);
        }
    }

    private final Map<UUID, Target> targets = new HashMap<UUID, Target>();

    public TransferData() {
        super(NAME);
    }

    public TransferData(String name) {
        super(name);
    }

    public static TransferData get(MinecraftServer server) {
        WorldServer overworld = server.getWorld(0);
        MapStorage storage = overworld.getMapStorage();
        TransferData data = (TransferData) storage.getOrLoadData(TransferData.class, NAME);
        if (data == null) {
            data = new TransferData();
            storage.setData(NAME, data);
        }
        return data;
    }

    public Target getTarget(UUID player) {
        return targets.get(player);
    }

    public void setBlockTarget(UUID player, int dimension, BlockPos pos) {
        targets.put(player, Target.block(dimension, pos));
        markDirty();
    }

    public void setItemTarget(UUID player, ResourceLocation item) {
        targets.put(player, Target.item(item));
        markDirty();
    }

    public void clear(UUID player) {
        if (targets.remove(player) != null) {
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        targets.clear();
        NBTTagList list = nbt.getTagList("targets", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            UUID player = entry.getUniqueId("player");
            if ("item".equals(entry.getString("type"))) {
                targets.put(player, Target.item(new ResourceLocation(entry.getString("item"))));
            } else {
                BlockPos pos = new BlockPos(entry.getInteger("x"), entry.getInteger("y"), entry.getInteger("z"));
                targets.put(player, Target.block(entry.getInteger("dimension"), pos));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, Target> mapEntry : targets.entrySet()) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setUniqueId("player", mapEntry.getKey());
            Target target = mapEntry.getValue();
            if (target.isItem) {
                entry.setString("type", "item");
                entry.setString("item", target.item.toString());
            } else {
                entry.setString("type", "block");
                entry.setInteger("dimension", target.dimension);
                entry.setInteger("x", target.pos.getX());
                entry.setInteger("y", target.pos.getY());
                entry.setInteger("z", target.pos.getZ());
            }
            list.appendTag(entry);
        }
        nbt.setTag("targets", list);
        return nbt;
    }
}
