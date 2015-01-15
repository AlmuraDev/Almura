/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.recipe.RecipeContainer;
import net.minecraft.item.crafting.IRecipe;

import java.util.Set;

public class RecipeNode implements INode<Set<RecipeContainer>> {

    private final Set<RecipeContainer> value;

    public RecipeNode(Set<RecipeContainer> value) {
        this.value = value;
    }

    @Override
    public Set<RecipeContainer> getValue() {
        return value;
    }
}
