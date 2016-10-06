/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.tabs.Tabs;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class Caches {


    public static final Block BLOCK_IRON_CACHE = GameRegistry.registerBlock(new CachesBlock("iron_Cache", "Iron Cache", 64000, Tabs.TAB_STORAGE)
            , CachesItemBlock.class, "iron_cache");

    // TODO test code
    public static final Block BLOCK_IRON_CACHE_2 = GameRegistry.registerBlock(new CachesBlock("iron_Cache2", "Iron Cache 2", 64, Tabs.TAB_STORAGE)
            , CachesItemBlock.class, "iron_cache2");

    private Caches() {}

    public static void init() {
        Almura.INTERNAL_PACK.addBlock(BLOCK_IRON_CACHE);
        Almura.INTERNAL_PACK.addBlock(BLOCK_IRON_CACHE_2);
    }
}
