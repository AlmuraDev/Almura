/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.content.type.block.SpecialBlockStateBlock;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlockImpl;
import com.almuradev.content.type.block.type.normal.NormalBlockImpl;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    CropBlockImpl.class,
    HorizontalBlockImpl.class,
    NormalBlockImpl.class
})
public class MixinSpecialBlockStateContentBlock implements SpecialBlockStateBlock {
    private ResourceLocation blockStateDefinitionLocation;

    @Override
    public ResourceLocation blockStateDefinitionLocation() {
        return this.blockStateDefinitionLocation;
    }

    @Override
    public void blockStateDefinitionLocation(final ResourceLocation location) {
        this.blockStateDefinitionLocation = location;
    }
}
