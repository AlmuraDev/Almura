/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache;

import com.almuradev.almura.mixin.ShapedRecipeAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * A cache made from the one below it, as 3.5's {@code almura:cache_shaped}: a shaped recipe whose result holds what the cache in it held.
 * Written as a shaped recipe ({@code "type": "almura:cache_upgrade"}).
 */
public final class CacheUpgradeRecipe extends ShapedRecipe {

    public static final RecipeSerializer<CacheUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(
        ShapedRecipe.MAP_CODEC.xmap(CacheUpgradeRecipe::new, recipe -> recipe),
        ShapedRecipe.STREAM_CODEC.map(CacheUpgradeRecipe::new, recipe -> recipe));

    private CacheUpgradeRecipe(final ShapedRecipe shaped) {
        super(new Recipe.CommonInfo(shaped.showNotification()), new CraftingRecipe.CraftingBookInfo(shaped.category(), shaped.group()),
            // Its result as written: no item stack can be made while recipes load
            shaped.pattern, ((ShapedRecipeAccessor) shaped).almura$result());
    }

    @Override
    public ItemStack assemble(final CraftingInput input) {
        final ItemStack result = super.assemble(input);
        for (final ItemStack ingredient : input.items()) {
            if (ingredient.getItem() instanceof final BlockItem item && item.getBlock() instanceof CacheBlock) {
                final CustomData contents = ingredient.get(DataComponents.CUSTOM_DATA);
                if (contents != null && contents.contains(CacheBlockEntity.ITEM_KEY)) {
                    result.set(DataComponents.CUSTOM_DATA, contents);
                }
                break;
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }
}
