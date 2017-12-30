/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup.mixin.impl;

import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import net.minecraft.block.SoundType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoundType.class)
public abstract class MixinSoundType implements BlockSoundGroup {

}
