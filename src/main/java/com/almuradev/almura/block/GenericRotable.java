/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block;

import com.google.common.base.Objects;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import org.spongepowered.api.CatalogType;

public final class GenericRotable extends BlockDirectional {

    public GenericRotable(Material materialIn) {
        super(materialIn);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", ((CatalogType) (Object) this).getId())
                .add("name", ((CatalogType) (Object) this).getName())
                .toString();
    }
}
