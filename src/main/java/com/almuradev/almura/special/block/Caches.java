/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import net.minecraft.creativetab.CreativeTabs;

public class Caches {

    // TODO test code
    public static final CachesBlock BLOCK_CACHES_64 = new CachesBlock("caches_64", "Cache (64)", 64, CreativeTabs.tabFood);
    public static final CachesBlock BLOCK_CACHES_128 = new CachesBlock("caches_128", "Cache (128)", 128, CreativeTabs.tabFood);

    private Caches() {}

    public static void fakeStaticLoad() {}
}
