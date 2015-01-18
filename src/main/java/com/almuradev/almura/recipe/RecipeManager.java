/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe;

import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.recipe.furnace.SmeltRecipes;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO Check each Minecraft update
public class RecipeManager {

    private static final List<ISmeltRecipe> SMELT_RECIPES = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    public static <R extends IRecipe> R registerRecipe(Pack pack, String owner, int id, Class<? extends R> clazz, ItemStack result, List params)
            throws UnknownRecipeTypeException, InvalidRecipeException, DuplicateRecipeException {
        R recipe = null;

        if (clazz == IShapedRecipe.class) {
            recipe = (R) createShapedRecipe(result, params.toArray(new Object[params.size()]));
        } else if (clazz == IShapelessRecipe.class) {
            recipe = (R) createShapelessRecipe(pack, owner, id, result, params.toArray(new Object[params.size()]));
        } else if (clazz == ISmeltRecipe.class) {
            recipe = (R) createSmeltRecipe(result, params.toArray(new Object[params.size()]));
        }

        if (recipe == null) {
            throw new UnknownRecipeTypeException(
                    "Recipe type [" + clazz.getSimpleName().toUpperCase() + "] is unknown. Valid types are [SHAPED, SHAPELESS, SMELT].");
        }

        final Optional<R> existing = findMatch(clazz, recipe);
        if (existing.isPresent()) {
            throw new DuplicateRecipeException(
                    "Recipe [" + id + "] of type [" + clazz.getSimpleName().toUpperCase() + "] in [" + owner + "] in pack [" + pack.getName()
                    + "] is a duplicate. Matches [" + existing.get() + "].");
        }

        if (clazz == IShapedRecipe.class) {
            CraftingManager.getInstance().getRecipeList().add(recipe);
        } else if (clazz == IShapelessRecipe.class) {
            CraftingManager.getInstance().getRecipeList().add(recipe);
        } else if (clazz == ISmeltRecipe.class) {
            FurnaceRecipes.smelting().func_151394_a(((ISmeltRecipe) recipe).getInput(), ((ISmeltRecipe) recipe).getOutput(),
                                                    ((ISmeltRecipe) recipe).getSmeltExperience());
            SMELT_RECIPES.add((ISmeltRecipe) recipe);
        }

        return recipe;
    }

    @SuppressWarnings("unchecked")
    public static <R extends IRecipe> Optional<R> findMatch(Class<? extends R> clazz, R recipe) {
        if (clazz == IShapedRecipe.class) {
            return (Optional<R>) findMatching((ShapedRecipes) recipe);
        } else if (clazz == IShapelessRecipe.class) {
            return (Optional<R>) findMatching((ShapelessRecipes) recipe);
        } else if (clazz == ISmeltRecipe.class) {
            return (Optional<R>) findMatching((SmeltRecipes) recipe);
        }
        return Optional.absent();
    }

    @SuppressWarnings("unchecked")
    private static ShapedRecipes createShapedRecipe(ItemStack result, Object... params) {
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
        return new ShapedRecipes(j, k, aitemstack, result);
    }

    private static ShapelessRecipes createShapelessRecipe(Pack pack, String owner, int id, ItemStack result, Object... params)
            throws InvalidRecipeException {
        final List<ItemStack> stacks = Lists.newArrayList();

        for (Object object1 : params) {
            if (object1 instanceof ItemStack) {
                stacks.add(((ItemStack) object1).copy());
            } else if (object1 instanceof Item) {
                stacks.add(new ItemStack((Item) object1));
            } else {
                if (!(object1 instanceof Block)) {
                    throw new InvalidRecipeException(
                            "Game Object [" + object1 + "] + in recipe [" + id + "] of type [SHAPELESS] in [" + owner + "] in pack [" + pack.getName()
                            + "] is not a block, item, or itemstack.");
                }
                stacks.add(new ItemStack((Block) object1));
            }
        }

        return new ShapelessRecipes(result, stacks);
    }

    private static SmeltRecipes createSmeltRecipe(ItemStack result, Object... params) {
        final ItemStack input = (ItemStack) params[0];
        final Float experience = (Float) params[1];

        return new SmeltRecipes(input, result, experience);
    }

    @SuppressWarnings("unchecked")
    private static Optional<? extends IRecipe> findMatching(ShapelessRecipes recipe) {
        for (Object registered : CraftingManager.getInstance().getRecipeList()) {
            final net.minecraft.item.crafting.IRecipe registeredRecipe = (net.minecraft.item.crafting.IRecipe) registered;

            if (!(registeredRecipe instanceof ShapelessRecipes)) {
                continue;
            }

            boolean doesMatch = true;
            final ShapelessRecipes registeredShapeless = (ShapelessRecipes) registered;
            if (registeredShapeless.recipeItems.size() == recipe.recipeItems.size()) {
                final ArrayList items = new ArrayList<>(recipe.recipeItems);

                for (Object uStack : registeredShapeless.recipeItems) {
                    final ItemStack unknownStack = (ItemStack) uStack;

                    for (Object rStack : recipe.recipeItems) {
                        final ItemStack recipeStack = (ItemStack) rStack;

                        // Air -> Air
                        if (unknownStack.getItem() == null && recipeStack.getItem() == null) {
                            items.remove(rStack);
                        }

                        // Air/Stack -> Air/Stack
                        if (unknownStack.getItem() == null || recipeStack.getItem() == null) {
                            continue;
                        }

                        // a. Items match
                        // b. Registered stack size is greater than or equal to recipe stack size
                        // c. Damage values match
                        if (unknownStack.getItem() == recipeStack.getItem() && unknownStack.stackSize != recipeStack.stackSize
                            && unknownStack.getItemDamage() == recipeStack.getItemDamage()) {
                            items.remove(rStack);
                        }
                    }

                    doesMatch = items.isEmpty();
                    if (doesMatch) {
                        break;
                    }
                }
            } else {
                doesMatch = false;
            }

            if (doesMatch) {
                return Optional.of((IRecipe) registeredShapeless);
            }
        }
        return Optional.absent();
    }

    private static Optional<? extends IRecipe> findMatching(ShapedRecipes recipe) {
        for (Object registered : CraftingManager.getInstance().getRecipeList()) {
            final net.minecraft.item.crafting.IRecipe registeredRecipe = (net.minecraft.item.crafting.IRecipe) registered;

            if (!(registeredRecipe instanceof ShapedRecipes)) {
                continue;
            }

            boolean doesMatch = true;

            final ShapedRecipes registeredShaped = (ShapedRecipes) registered;
            if (registeredShaped.recipeItems.length == recipe.recipeItems.length) {
                for (int i = 0; i < registeredShaped.recipeItems.length; i++) {
                    final ItemStack unknownStack = ((ShapedRecipes) registeredRecipe).recipeItems[i];
                    final ItemStack recipeStack = recipe.recipeItems[i];

                    // Air -> Air
                    if (unknownStack == null && recipeStack == null) {
                        continue;
                    }

                    // Air/Stack -> Air/Stack
                    if (unknownStack == null || recipeStack == null) {
                        doesMatch = false;
                        break;
                    }

                    // a. Items don't match
                    // b. Registered stack size is less than recipe stack size
                    // c. Damage values don't match
                    if (unknownStack.getItem() != recipeStack.getItem() || unknownStack.stackSize != recipeStack.stackSize
                        || unknownStack.getItemDamage() != recipeStack.getItemDamage()) {
                        doesMatch = false;
                        break;
                    }
                }

            } else {
                doesMatch = false;
            }

            if (doesMatch) {
                return Optional.of((IShapedRecipe) registeredShaped);
            }
        }
        return Optional.absent();
    }

    private static Optional<? extends IRecipe> findMatching(SmeltRecipes recipe) {
        //Check cache first
        for (ISmeltRecipe registered : SMELT_RECIPES) {
            if (registered.equals(recipe)) {
                return Optional.of(registered);
            }
        }

        final ItemStack resultStack = FurnaceRecipes.smelting().getSmeltingResult(recipe.getInput());
        if (resultStack != null) {
            final float experience = FurnaceRecipes.smelting().func_151398_b(recipe.getInput());
            SmeltRecipes existing = new SmeltRecipes(recipe.getInput(), resultStack, experience);
            SMELT_RECIPES.add(existing);
            return Optional.of(existing);
        }

        return Optional.absent();
    }
}
