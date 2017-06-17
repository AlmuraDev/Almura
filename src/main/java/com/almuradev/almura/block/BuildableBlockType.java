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
import java.util.OptionalDouble;
import java.util.OptionalInt;

public interface BuildableBlockType extends MaterialType, BlockType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<BLOCK extends BuildableBlockType, BUILDER extends Builder<BLOCK, BUILDER>> extends MaterialType.Builder<BLOCK, BUILDER> {

        Optional<Material> material();

        BUILDER material(Material material);

        Optional<MapColor> mapColor();

        BUILDER mapColor(MapColor mapColor);

        OptionalDouble hardness();

        BUILDER hardness(float hardness);

        OptionalDouble lightEmission();

        BUILDER lightEmission(float lightEmission);

        OptionalInt lightOpacity();

        BUILDER lightOpacity(int lightOpacity);

        OptionalDouble resistance();

        BUILDER resistance(float resistance);

        @Override
        default BLOCK build(String id, String name) {
            return build(id);
        }

        @Override
        BLOCK build(String id);
    }
}
