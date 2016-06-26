/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.pack.PackObject;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.Sponge;

public interface PackBlockObject extends PackObject {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<P extends PackBlockObject, B extends Builder<P, B>> extends PackObject.Builder<P, B> {

        Builder<P, B> material(Material material);

        Builder<P, B> mapColor(MapColor mapColor);

        Builder<P, B> creativeTab(CreativeTabs tab);

        @Override
        P build(String id, String name);
    }
}
