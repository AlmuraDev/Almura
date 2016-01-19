/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.recipe.IRecipe;
import com.almuradev.almura.recipe.RecipeContainer;

import java.util.Set;

public class RecipeNode implements INode<Set<RecipeContainer<? extends IRecipe>>> {

    private final Set<RecipeContainer<? extends IRecipe>> value;

    public RecipeNode(Set<RecipeContainer<? extends IRecipe>> value) {
        this.value = value;
    }

    @Override
    public Set<RecipeContainer<? extends IRecipe>> getValue() {
        return value;
    }
}
