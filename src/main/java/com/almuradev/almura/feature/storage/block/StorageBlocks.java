/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage.block;

import com.almuradev.almura.Almura;
import net.minecraft.util.ResourceLocation;

public final class StorageBlocks {
    public static final StorageBlock DOCK_CHEST = new StorageBlock(new ResourceLocation(Almura.ID, "storage/dock_chest"), 54);
    public static final StorageBlock QUANTUM_CHEST = new StorageBlock(new ResourceLocation(Almura.ID, "storage/quantum_chest"), 54);
}
