package com.sxilverr.lootall.fabric.mixin;

import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if >=1.21.1 {
/*import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
*///?} else {
import net.minecraft.resources.ResourceLocation;
//?}

@Mixin(RandomizableContainerBlockEntity.class)
public interface RandomizableContainerAccessor {
    //? if >=1.21.1 {
    /*@Accessor("lootTable")
    ResourceKey<LootTable> lootall$getLootTable();
    *///?} else {
    @Accessor("lootTable")
    ResourceLocation lootall$getLootTable();
    //?}
}
