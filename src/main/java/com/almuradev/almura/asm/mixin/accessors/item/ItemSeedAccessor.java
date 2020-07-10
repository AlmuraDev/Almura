/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeeds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemSeeds.class)
public interface ItemSeedAccessor {
    //public-f net.minecraft.item.ItemSeeds field_77838_b # soilBlockID
    @Mutable @Accessor("soilBlockID") Block accessor$getSoilBlockID();
    @Mutable @Accessor("soilBlockID") void accessor$setSoilBlockID(Block block);

    //public-f net.minecraft.item.ItemSeeds field_150925_a # crops
    @Mutable @Accessor("crops") Block accessor$getCrops();
    @Mutable @Accessor("crops") void accessor$setCrops(Block block);
}
