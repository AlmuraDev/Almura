/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.block;

import com.almuradev.almura.pack.block.PackBlockObject;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class MixinBlock extends net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl<Block> implements PackBlockObject {

}
