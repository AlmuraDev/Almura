/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.block;

import com.almuradev.almura.BuildableCatalogType;
import com.almuradev.almura.api.CreativeTab;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;

public interface BuildableBlockType extends BuildableCatalogType, BlockType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<BLOCK extends BuildableBlockType, BUILDER extends Builder<BLOCK, BUILDER>>
            extends BuildableCatalogType.Builder<BLOCK, BUILDER> {

        Builder<BLOCK, BUILDER> unlocalizedName(String dictName);

        Builder<BLOCK, BUILDER> material(Material material);

        Builder<BLOCK, BUILDER> mapColor(MapColor mapColor);

        Builder<BLOCK, BUILDER> hardness(float hardness);

        Builder<BLOCK, BUILDER> resistance(float resistance);

        Builder<BLOCK, BUILDER> creativeTab(CreativeTab tab);

        @Override
        default BLOCK build(String id, String name) {
            return build(id);
        }

        BLOCK build(String id);
    }
}
