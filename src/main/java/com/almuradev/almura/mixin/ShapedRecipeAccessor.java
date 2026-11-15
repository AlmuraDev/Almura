/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * A shaped recipe's result as written (26.3 keeps it private): the caches' upgrade recipes copy it while recipes load, when no item stack
 * can be made yet.
 */
@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {

    @Accessor("result")
    ItemStackTemplate almura$result();
}
