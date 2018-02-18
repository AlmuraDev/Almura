/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.block;

import com.almuradev.almura.Almura;
import net.minecraft.util.ResourceLocation;

public class CacheBlocks {
    public static CacheBlock WOOD = new CacheBlock(new ResourceLocation(Almura.ID, "cache/wood"), 640);
    public static CacheBlock IRON = new CacheBlock(new ResourceLocation(Almura.ID, "cache/iron"), 6400);
    public static CacheBlock GOLD = new CacheBlock(new ResourceLocation(Almura.ID, "cache/gold"), 64000);
    public static CacheBlock DIAMOND = new CacheBlock(new ResourceLocation(Almura.ID, "cache/diamond"), 640000);
    public static CacheBlock NETHER = new CacheBlock(new ResourceLocation(Almura.ID, "cache/nether"), 6400000);
    public static CacheBlock ENDER = new CacheBlock(new ResourceLocation(Almura.ID, "cache/ender"), 64000000);
}
