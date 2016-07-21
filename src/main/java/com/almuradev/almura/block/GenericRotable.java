/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block;

import com.almuradev.almura.block.builder.rotable.AbstractRotableTypeBuilder;
import com.almuradev.almura.mixin.interfaces.IMixinBuildableBlockType;
import com.almuradev.almura.mixin.interfaces.IMixinRotableBlockType;
import com.google.common.base.Objects;
import net.minecraft.block.BlockDirectional;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.CatalogType;

public final class GenericRotable extends BlockDirectional {

    public GenericRotable(String modid, String id, AbstractRotableTypeBuilder<?, ?> builder) {
        super(builder.material);
        this.setRegistryName(modid, id);
        ((IMixinRotableBlockType) (Object) this).setMapColor(builder.mapColor);
        this.setCreativeTab((CreativeTabs) builder.tab);
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
