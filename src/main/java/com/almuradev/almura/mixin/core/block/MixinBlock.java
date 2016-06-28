/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.block;

import com.almuradev.almura.api.block.BuildableBlockType;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;

// Makes all blocks BuildableBlockTypes (so they can be used in Almura's framework)
@Mixin(Block.class)
public abstract class MixinBlock extends net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl<Block> implements BuildableBlockType {

}
