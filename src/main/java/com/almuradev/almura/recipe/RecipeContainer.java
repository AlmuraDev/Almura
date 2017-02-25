/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe;

import com.almuradev.almura.pack.Pack;

public class RecipeContainer<R extends IRecipe> {

    private final Pack pack;
    private final String owner;
    private final int id;
    private final R recipe;

    public RecipeContainer(Pack pack, String owner, int id, R recipe) {
        this.pack = pack;
        this.owner = owner;
        this.id = id;
        this.recipe = recipe;
    }

    public Pack getPack() {
        return pack;
    }

    public String getOwner() {
        return owner;
    }

    public int getId() {
        return id;
    }

    public R getRecipe() {
        return recipe;
    }
}
