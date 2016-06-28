/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.block;

import com.almuradev.almura.block.GenericRotable;
import com.almuradev.almura.mixin.interfaces.IMixinRotableBlockType;
import net.minecraft.block.material.MapColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = {GenericRotable.class})
public abstract class MixinRotableBlockType implements IMixinRotableBlockType {

    @Shadow protected MapColor blockMapColor;

    @Override
    public void setMapColor(MapColor mapColor) {
        this.blockMapColor = mapColor;
    }
}
