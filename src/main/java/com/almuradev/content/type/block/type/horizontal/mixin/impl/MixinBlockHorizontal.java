/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal.mixin.impl;

import com.almuradev.content.type.block.type.horizontal.HorizontalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Implements(@Interface(iface = HorizontalBlock.class, prefix = "rotatable$"))
@Mixin(value = BlockHorizontal.class, priority = 999)
public abstract class MixinBlockHorizontal extends Block {

    public MixinBlockHorizontal(final Material material, final MapColor color) {
        super(material, color);
    }
}
