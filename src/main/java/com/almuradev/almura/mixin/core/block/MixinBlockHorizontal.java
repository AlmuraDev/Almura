/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.block;

import com.almuradev.almura.api.block.rotatable.HorizontalBlockType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

// Makes all horizontals RotableBlockTypes (so they can be used in Almura's framework)
@Mixin(BlockHorizontal.class)
@Implements(value = @Interface(iface = HorizontalBlockType.class, prefix = "rotable$"))
public abstract class MixinBlockHorizontal extends Block {

    // ignore
    public MixinBlockHorizontal(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }
}
