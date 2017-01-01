/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block;

import com.almuradev.almura.MaterialType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;

import java.util.Optional;

public interface BuildableBlockType extends MaterialType, BlockType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<BLOCK extends BuildableBlockType, BUILDER extends Builder<BLOCK, BUILDER>> extends MaterialType.Builder<BLOCK, BUILDER> {

        BUILDER material(Material material);

        Optional<Material> material();

        BUILDER mapColor(MapColor mapColor);

        Optional<MapColor> mapColor();

        BUILDER hardness(float hardness);

        float hardness();

        BUILDER resistance(float resistance);

        float resistance();

        @Override
        default BLOCK build(String id, String name) {
            return build(id);
        }

        BLOCK build(String id);
    }
}
