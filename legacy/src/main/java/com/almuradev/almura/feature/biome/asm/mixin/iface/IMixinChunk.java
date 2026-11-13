/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.iface;

public interface IMixinChunk {

    int[] cacheRealBiomeIds();

    int[] getExtendedBiomeArray();
}
