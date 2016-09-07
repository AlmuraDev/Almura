/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe;

import net.minecraft.item.ItemStack;

public interface ISmeltRecipe extends IRecipe {

    ItemStack getInput();

    ItemStack getOutput();

    float getSmeltExperience();
}
