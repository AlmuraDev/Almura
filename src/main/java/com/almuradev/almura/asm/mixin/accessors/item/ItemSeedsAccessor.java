/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSeeds;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemSeeds.class)
public interface ItemSeedsAccessor {
    //public-f net.minecraft.item.ItemSeeds field_77838_b # soilBlockID
    @Accessor("soilBlockID") Block accessor$getSoilBlockID();
    @Final @Accessor("soilBlockID") void accessor$setSoilBlockID(Block block);

    //public-f net.minecraft.item.ItemSeeds field_150925_a # crops
    @Accessor("crops") Block accessor$getCrops();
    @Final @Accessor("crops") void accessor$setCrops(Block block);
}
