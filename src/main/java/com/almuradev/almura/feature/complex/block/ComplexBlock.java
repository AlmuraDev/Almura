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
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;

import javax.annotation.Nullable;

public final class ComplexBlock extends BlockContainer {

    public ComplexBlock(ResourceLocation registryName, float hardness, float resistance) {
        super(Material.GROUND);
        this.setRegistryName(registryName);
        this.setUnlocalizedName(registryName.getResourcePath().replace('/', '.'));
        this.setHardness(hardness);
        this.setResistance(resistance);

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":machine").ifPresent((itemGroup) -> setCreativeTab((CreativeTabs) itemGroup));
    }

    public ComplexBlock(Material materialIn) {
        super(materialIn);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }
}
