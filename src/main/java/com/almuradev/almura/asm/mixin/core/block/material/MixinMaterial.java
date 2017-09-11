/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block.material;

import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Material.class)
public abstract class MixinMaterial implements com.almuradev.almura.content.type.material.Material {

}
