/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.block;

import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import net.minecraft.block.SoundType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoundType.class)
public abstract class MixinSoundType implements BlockSoundGroup {

}
