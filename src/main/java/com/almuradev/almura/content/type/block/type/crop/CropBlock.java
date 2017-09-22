/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.crop;

import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockCrops;
import org.spongepowered.api.CatalogType;

public final class CropBlock extends BlockCrops {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((CatalogType) (Object) this).getId())
                .add("name", ((CatalogType) (Object) this).getName())
                .toString();
    }
}
