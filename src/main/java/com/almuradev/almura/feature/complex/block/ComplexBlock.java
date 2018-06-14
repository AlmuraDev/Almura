/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.block;

import com.almuradev.almura.Almura;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.Sponge;

public final class ComplexBlock extends Block {

    public ComplexBlock(ResourceLocation registryName) {
        super(Material.GROUND);
        this.setRegistryName(registryName);
        this.setUnlocalizedName(registryName.getResourcePath().replace('/', '.'));
        this.setHardness(3.0F);
        this.setResistance(1000.0F);

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":machine").ifPresent((itemGroup) -> setCreativeTab((CreativeTabs) itemGroup));
    }

    public ComplexBlock(Material materialIn) {
        super(materialIn);
    }
}
