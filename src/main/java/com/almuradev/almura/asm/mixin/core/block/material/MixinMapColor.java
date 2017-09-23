/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.block.material;

import net.minecraft.block.material.MapColor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MapColor.class)
public abstract class MixinMapColor implements com.almuradev.almura.content.type.material.MapColor {

}
