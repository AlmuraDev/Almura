/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block;

import com.almuradev.almura.block.builder.AbstractBlockTypeBuilder;
import com.almuradev.almura.mixin.interfaces.IMixinBuildableBlockType;
import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.CatalogType;

public final class GenericBlock extends Block {

    public GenericBlock(String modid, String id, AbstractBlockTypeBuilder<?, ?> builder) {
        super(builder.material, builder.mapColor);
        this.setRegistryName(modid, id);
        this.setUnlocalizedName(builder.dictName);
        this.setCreativeTab((CreativeTabs) builder.tab);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", ((CatalogType) (Object) this).getId())
                .add("unlocalizedName", this.getUnlocalizedName())
                .add("material", this.blockMaterial)
                .add("mapColor", this.blockMapColor)
                .add("creativeTab", ((IMixinBuildableBlockType) (Object) this).getCreativeTab())
                .toString();
    }
}
