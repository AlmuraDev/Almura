/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.asm.mixin.block.material;

import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Material.class)
public abstract class MixinMaterial implements com.almuradev.content.type.material.Material {

}
