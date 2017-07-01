/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block;

import com.almuradev.almura.content.block.rotatable.HorizontalType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

// Makes all horizontals RotableBlockTypes (so they can be used in Almura's framework)
@Mixin(value = BlockHorizontal.class, priority = 999)
@Implements(value = @Interface(iface = HorizontalType.class, prefix = "rotatable$"))
public abstract class MixinBlockHorizontal extends Block {

    // ignore
    public MixinBlockHorizontal(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }
}
