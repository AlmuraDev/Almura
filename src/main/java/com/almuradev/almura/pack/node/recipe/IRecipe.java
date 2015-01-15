/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.recipe;

import com.almuradev.almura.pack.Pack;

public interface IRecipe {

    Pack getPack();

    String getOwner();

    int getId();
}
