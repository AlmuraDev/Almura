/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.recipe;

import com.almuradev.almura.asm.mixin.accessors.item.crafting.ShapedRecipesAccessor;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.Map;

@SuppressWarnings("unused")
public final class CacheRecipeFactory implements IRecipeFactory {

    private static final ResourceLocation SHAPED_CRAFTING_PARSER_NAME = new ResourceLocation("minecraft:crafting_shaped");
    private final IRecipeFactory vanillaShapedCraftingFactory;

    /**
     * This constructor is called by forge via reflection.
     */
    public CacheRecipeFactory() {
        Map<ResourceLocation, IRecipeFactory> map = ReflectionHelper.getPrivateValue(CraftingHelper.class, null, 4);
        this.vanillaShapedCraftingFactory = map.get(SHAPED_CRAFTING_PARSER_NAME);
    }

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        final ShapedRecipes vanilla = (ShapedRecipes) this.vanillaShapedCraftingFactory.parse(context, json);
        return new CacheRecipe(((ShapedRecipesAccessor)vanilla).accessor$getGroup(), vanilla.getWidth(), vanilla.getHeight(), ((ShapedRecipesAccessor)vanilla).accessor$getRecipeItems(), vanilla.getRecipeOutput());
    }
}
