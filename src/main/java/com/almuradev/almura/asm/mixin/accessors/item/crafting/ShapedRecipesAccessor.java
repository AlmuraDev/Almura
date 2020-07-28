/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.item.crafting;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipes.class)
public interface ShapedRecipesAccessor {
    //public net.minecraft.item.crafting.ShapedRecipes field_194137_e # group
    @Accessor("group") String accessor$getGroup();
    //New Entry
    @Accessor("recipeItems") NonNullList<Ingredient> accessor$getRecipeItems();
}
