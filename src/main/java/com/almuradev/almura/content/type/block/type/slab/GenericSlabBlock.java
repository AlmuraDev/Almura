/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.type.slab;

import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.CatalogType;

public class GenericSlabBlock extends BlockSlab {

    public GenericSlabBlock(Material material) {
        super(material);
    }

    @Override
    public String getUnlocalizedName(int meta) {
        return null;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return null;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", ((CatalogType) (Object) this).getId())
                .add("name", ((CatalogType) (Object) this).getName())
                .toString();
    }
}
