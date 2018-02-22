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
    public static final StorageBlock OAK_STORAGE_CRATE = new StorageBlock(new ResourceLocation(Almura.ID, "storage/oak_storage_crate"), 54);
    public static final StorageBlock SPRUCE_STORAGE_CRATE = new StorageBlock(new ResourceLocation(Almura.ID, "storage/spruce_storage_crate"), 54);
    public static final StorageBlock BITCH_STORAGE_CRATE = new StorageBlock(new ResourceLocation(Almura.ID, "storage/birch_storage_crate"), 54);
}
