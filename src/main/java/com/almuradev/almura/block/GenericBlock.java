/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block;

import com.almuradev.almura.mixin.interfaces.IMixinBuildableBlockType;
import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import org.spongepowered.api.CatalogType;

public final class GenericBlock extends Block {

    public GenericBlock(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public GenericBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", ((CatalogType) (Object) this).getId())
                .add("name", ((CatalogType) (Object) this).getName())
                .add("material", this.blockMaterial)
                .add("mapColor", this.blockMapColor)
                .add("creativeTab", ((IMixinBuildableBlockType) (Object) this).getCreativeTab())
                .toString();
    }
}
