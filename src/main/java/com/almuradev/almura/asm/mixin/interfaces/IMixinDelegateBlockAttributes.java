/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.registry.CatalogDelegate;

public interface IMixinDelegateBlockAttributes {

    void setSoundGroupDelegate(final CatalogDelegate<BlockSoundGroup> delegate);
}
