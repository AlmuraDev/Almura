/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe;

import net.minecraft.item.ItemStack;

public interface ISmeltingRecipe extends IRecipe {

    ItemStack getInput();

    ItemStack getOutput();

    float getSmeltExperience();

    boolean matches(ItemStack input, ItemStack output);
}
