/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe;

import com.almuradev.almura.recipe.furnace.SmeltRecipes;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO Check each Minecraft update
public class RecipeManager {
    private static final List<ISmeltRecipe> SMELT_RECIPES = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    public static <R extends IRecipe> R registerRecipe(Class<? extends R> clazz, ItemStack result, Object... params) throws UnknownRecipeTypeException, InvalidRecipeException, DuplicateRecipeException {
        R recipe = null;

        if (clazz == IShapedRecipe.class) {
            recipe = (R) createShapedRecipe(result, params);
        } else if (clazz == IShapelessRecipe.class) {
            recipe = (R) createShapelessRecipe(result, params);
        } else if (clazz == ISmeltRecipe.class) {
            recipe = (R) createSmeltRecipe(result, params);
        }

        if (recipe == null) {
            throw new UnknownRecipeTypeException("Recipe type [" + clazz + "] is an unknown recipe type. Valid types are [SHAPED, SHAPELESS, SMELT]");
        }

        final Optional<R> existing = findMatch(clazz, recipe);
        if (existing.isPresent()) {
            //TODO Exception handling
            throw new DuplicateRecipeException("");
        }

        if (clazz == IShapedRecipe.class) {
            CraftingManager.getInstance().getRecipeList().add(recipe);
        } else if (clazz == IShapelessRecipe.class) {
            CraftingManager.getInstance().getRecipeList().add(recipe);
        } else if (clazz == ISmeltRecipe.class) {
            SMELT_RECIPES.add((ISmeltRecipe) recipe);
        }

        return recipe;
    }

    @SuppressWarnings("unchecked")
    public static <R extends IRecipe> Optional<R> findMatch(Class<? extends R> clazz, R recipe) {
        if (clazz == IShapelessRecipe.class) {
            return Optional.fromNullable((R) findMatching((ShapedRecipes) recipe));
        } else if (clazz == IShapelessRecipe.class) {
            return Optional.fromNullable((R) findMatching((ShapelessRecipes) recipe));
        } else if (clazz == ISmeltRecipe.class) {
            return Optional.fromNullable((R) findMatching((SmeltRecipes) recipe));
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

    private static ShapelessRecipes createShapelessRecipe(ItemStack result, Object... params) throws InvalidRecipeException {
        final List<ItemStack> stacks = Lists.newArrayList();

        for (Object object1 : params) {
            if (object1 instanceof ItemStack) {
                stacks.add(((ItemStack) object1).copy());
            } else if (object1 instanceof Item) {
                stacks.add(new ItemStack((Item) object1));
            } else {
                if (!(object1 instanceof Block)) {
                    throw new InvalidRecipeException("Object [" + object1 + "] is not a Block, Item, or ItemStack.");
                }
                stacks.add(new ItemStack((Block) object1));
            }
        }

        return new ShapelessRecipes(result, stacks);
    }

    private static SmeltRecipes createSmeltRecipe(ItemStack result, Object params) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Optional<ShapelessRecipes> findMatching(ShapelessRecipes recipe) {
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
                return Optional.of(registeredShapeless);
            }
        }
        return Optional.absent();
    }

    private static Optional<ShapedRecipes> findMatching(ShapedRecipes recipe) {
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
                return Optional.of(registeredShaped);
            }
        }
        return Optional.absent();
    }

    private static Optional<SmeltRecipes> findMatching(SmeltRecipes recipe) {
        return null;
    }
}
