/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.asm.mixin.item;

import com.almuradev.content.type.item.type.seed.SeedItem;
import net.minecraft.item.ItemSeeds;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ItemSeeds.class, priority = 999)
public abstract class MixinItemSeeds extends MixinItem implements SeedItem {

}
