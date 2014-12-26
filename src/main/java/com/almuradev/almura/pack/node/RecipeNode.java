package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.recipe.RecipeContainer;
import net.minecraft.item.crafting.IRecipe;

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
