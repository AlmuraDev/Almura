/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block.material;

import net.minecraft.block.material.MapColor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MapColor.class)
public abstract class MixinMapColor implements com.almuradev.almura.content.type.material.MapColor {

}
