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
import net.minecraft.item.crafting.ShapelessRecipes;

import java.util.HashMap;
import java.util.List;

public class RecipeContainer<T extends IRecipe> {

    private final Pack pack;
    private final String identifier;
    private final int id;
    private final T recipe;

    @SuppressWarnings("unchecked")
    public RecipeContainer(Pack pack, String name, Class<T> clazz, int id, ItemStack result, List<Object> params)
            throws UnknownRecipeTypeException, InvalidRecipeException {
        this.pack = pack;
        this.identifier = name;
        this.id = id;
        try {
            if (clazz == QuantitiveShapedRecipes.class) {
                recipe = (T) createShapedRecipe(result, params.toArray(new Object[params.size()]));
                CraftingManager.getInstance().getRecipeList().add(recipe);
            } else if (clazz == QuantitiveShapelessRecipes.class) {
                recipe = (T) createShapelessRecipe(result, params.toArray(new Object[params.size()]));
                CraftingManager.getInstance().getRecipeList().add(recipe);
            } else {
                throw new UnknownRecipeTypeException(
                        "Recipe type [" + clazz.getSimpleName() + "] with id [" + id + "] in [" + name + "] in pack [" + pack.getName()
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
    @SuppressWarnings("unchecked")
    public QuantitiveShapedRecipes createShapedRecipe(ItemStack result, Object... params) {
        String s = "";
        int i = 0;
        int j = 0;
        int k = 0;

        if (params[i] instanceof String[]) {
            String[] astring = (String[]) params[i++];

            for (String s1 : astring) {
                ++k;
                j = s1.length();
                s = s + s1;
            }
        } else {
            while (params[i] instanceof String) {
                String s2 = (String) params[i++];
                ++k;
                j = s2.length();
                s = s + s2;
            }
        }

        HashMap hashmap;

        for (hashmap = new HashMap(); i < params.length; i += 2) {
            Character character = (Character) params[i];
            ItemStack itemstack1 = null;

            if (params[i + 1] instanceof Item) {
                itemstack1 = new ItemStack((Item) params[i + 1]);
            } else if (params[i + 1] instanceof Block) {
                itemstack1 = new ItemStack((Block) params[i + 1], 1, 32767);
            } else if (params[i + 1] instanceof ItemStack) {
                itemstack1 = (ItemStack) params[i + 1];
            }

            hashmap.put(character, itemstack1);
        }

        ItemStack[] aitemstack = new ItemStack[j * k];

        for (int i1 = 0; i1 < j * k; ++i1) {
            char c0 = s.charAt(i1);

            if (hashmap.containsKey(Character.valueOf(c0))) {
                aitemstack[i1] = ((ItemStack) hashmap.get(Character.valueOf(c0))).copy();
            } else {
                aitemstack[i1] = null;
            }
        }

        return new QuantitiveShapedRecipes(j, k, aitemstack, result);
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
