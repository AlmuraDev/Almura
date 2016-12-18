/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.block;

import com.almuradev.almura.api.BuildableCatalogType;
import com.almuradev.almura.api.creativetab.CreativeTab;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;

public interface BuildableBlockType extends BuildableCatalogType, BlockType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(BuildableBlockType.Builder.class);
    }

    interface Builder<BLOCK extends BuildableBlockType, BUILDER extends Builder<BLOCK, BUILDER>>
            extends BuildableCatalogType.Builder<BLOCK, BUILDER> {

        BUILDER unlocalizedName(String dictName);

        BUILDER material(Material material);

        BUILDER mapColor(MapColor mapColor);

        BUILDER hardness(float hardness);

        BUILDER resistance(float resistance);

        BUILDER creativeTab(CreativeTab tab);

        @Override
        default BLOCK build(String id, String name) {
            return build(id);
        }

        BLOCK build(String id);
    }
}
