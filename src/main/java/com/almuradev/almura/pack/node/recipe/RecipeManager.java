/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.util.Set;

public class RecipeManager {
    private static final Set<ISmeltingRecipe> SMELTING_RECIPES = Sets.newHashSet();

    public static <R extends IRecipe> R registerRecipe(Class<R> clazz, Object... params) {
        return null;
    }

    public static <R extends IRecipe> Optional<R> findRecipe(Class<R> clazz, Object... params) {
        return Optional.absent();
    }
}
