/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import com.almuradev.almura.asm.mixin.accessors.item.crafting.ShapedRecipesAccessor;
import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.Map;

/**
 * {@code almura:shaped_consume_tools}: a shaped recipe that uses up every ingredient, including tools that normally come back from
 * crafting (the knife built into the kitchen slicer, the grinder into the kitchen grinder).
 */
@SuppressWarnings("unused")
public final class ConsumeToolsRecipeFactory implements IRecipeFactory {

    private static final ResourceLocation SHAPED_CRAFTING_PARSER_NAME = new ResourceLocation("minecraft:crafting_shaped");
    private final IRecipeFactory vanillaShapedCraftingFactory;

    /**
     * Called by Forge via reflection.
     */
    public ConsumeToolsRecipeFactory() {
        final Map<ResourceLocation, IRecipeFactory> map = ReflectionHelper.getPrivateValue(CraftingHelper.class, null, 4);
        this.vanillaShapedCraftingFactory = map.get(SHAPED_CRAFTING_PARSER_NAME);
    }

    @Override
    public IRecipe parse(final JsonContext context, final JsonObject json) {
        final ShapedRecipes vanilla = (ShapedRecipes) this.vanillaShapedCraftingFactory.parse(context, json);
        return new ShapedRecipes(((ShapedRecipesAccessor) vanilla).accessor$getGroup(), vanilla.getWidth(), vanilla.getHeight(),
                ((ShapedRecipesAccessor) vanilla).accessor$getRecipeItems(), vanilla.getRecipeOutput()) {
            @Override
            public NonNullList<ItemStack> getRemainingItems(final InventoryCrafting inventory) {
                return NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
            }
        };
    }
}
