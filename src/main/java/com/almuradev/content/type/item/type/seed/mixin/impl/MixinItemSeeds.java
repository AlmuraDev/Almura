/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed.mixin.impl;

import com.almuradev.content.type.item.mixin.impl.MixinItem;
import com.almuradev.content.type.item.type.seed.SeedItem;
import com.almuradev.content.type.item.type.seed.processor.grass.Grass;
import net.minecraft.item.ItemSeeds;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ItemSeeds.class, priority = 999)
public abstract class MixinItemSeeds extends MixinItem implements SeedItem {

    // only Almura seeds have grass
    @Override
    public Grass getGrass() {
        return null;
    }
}
