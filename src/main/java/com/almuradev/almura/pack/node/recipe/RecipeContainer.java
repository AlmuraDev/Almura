/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe;

import com.almuradev.almura.pack.Pack;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import java.util.List;

public class RecipeContainer<T extends IRecipe> {

    private final Pack pack;
    private final String identifier;
    private final int id;
    private final T recipe;

    @SuppressWarnings("unchecked")
    public RecipeContainer(Pack pack, String identifier, Class<T> clazz, int id, ItemStack result, List<Object> params)
            throws UnknownRecipeTypeException, InvalidRecipeException {
        this.pack = pack;
        this.identifier = identifier;
        this.id = id;
        try {
            if (clazz == ShapedRecipes.class) {
                recipe = (T) CraftingManager.getInstance().addRecipe(result, params.toArray(new Object[params.size()]));
            } else if (clazz == ShapelessRecipes.class) {
                recipe = (T) createShapelessRecipe(result, params.toArray(new Object[params.size()]));
                CraftingManager.getInstance().getRecipeList().add(recipe);
            } else {
                throw new UnknownRecipeTypeException(
                        "Recipe type [" + clazz.getSimpleName() + "] with id [" + id + "] in [" + identifier + "] in pack [" + pack.getName()
                        + "] is not valid. Valid types are [SHAPED, SHAPELESS].");
            }
        } catch (RuntimeException ex) {
            throw new InvalidRecipeException(ex);
        }
    }

    public Pack getPack() {
        return pack;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getId() {
        return id;
    }

    public T getRecipe() {
        return recipe;
    }

    //TODO Check each Minecraft update
    private ShapelessRecipes createShapelessRecipe(ItemStack result, Object... params) {
        final List<ItemStack> stacks = Lists.newArrayList();

        for (Object object1 : params) {
            if (object1 instanceof ItemStack) {
                stacks.add(((ItemStack) object1).copy());
            } else if (object1 instanceof Item) {
                stacks.add(new ItemStack((Item) object1));
            } else {
                if (!(object1 instanceof Block)) {
                    throw new RuntimeException("Invalid shapeless recipe!");
                }
                stacks.add(new ItemStack((Block) object1));
            }
        }

        return new ShapelessRecipes(result, stacks);
    }
}
