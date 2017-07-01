/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block;

import com.almuradev.almura.content.block.CropType;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

// Makes all crops CropTypes (so they can be used in Almura's framework)
@Mixin(value = BlockCrops.class, priority = 999)
@Implements(@Interface(iface = CropType.class, prefix = "crop$"))
public abstract class MixinBlockCrops extends BlockBush {

}
