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

    // TODO test code
    public static final Block BLOCK_IRON_CACHE = GameRegistry.registerBlock(new CachesBlock("iron_cache_6400", "iron_cache", "Iron Cache", 6400, Tabs.TAB_STORAGE), CachesItemBlock.class, "iron_cache_6400");
    public static final Block BLOCK_GOLD_CACHE = GameRegistry.registerBlock(new CachesBlock("gold_cache_64000", "gold_cache", "Gold Cache", 64000, Tabs.TAB_STORAGE), CachesItemBlock.class, "gold_cache_64000");
    public static final Block BLOCK_EMERALD_CACHE = GameRegistry.registerBlock(new CachesBlock("emerald_cache_128000", "emerald_cache", "Emerald Cache", 128000, Tabs.TAB_STORAGE), CachesItemBlock.class, "emerald_cache_128000");
    public static final Block BLOCK_DIAMOND_CACHE = GameRegistry.registerBlock(new CachesBlock("diamond_cache_256000", "diamond_cache", "Diamond Cache", 256000, Tabs.TAB_STORAGE), CachesItemBlock.class, "diamond_cache_256000");
    public static final Block BLOCK_REDSTONE_CACHE = GameRegistry.registerBlock(new CachesBlock("redstone_cache_200000000", "redstone_cache", "Redstone Cache", 200000000, Tabs.TAB_STORAGE), CachesItemBlock.class, "redstone_cache_200000000");

    private Caches() {}

    public static void init() {
        Almura.INTERNAL_PACK.addBlock(BLOCK_IRON_CACHE);
        Almura.INTERNAL_PACK.addBlock(BLOCK_GOLD_CACHE);
        Almura.INTERNAL_PACK.addBlock(BLOCK_EMERALD_CACHE);
        Almura.INTERNAL_PACK.addBlock(BLOCK_DIAMOND_CACHE);
        Almura.INTERNAL_PACK.addBlock(BLOCK_REDSTONE_CACHE);
    }
}
