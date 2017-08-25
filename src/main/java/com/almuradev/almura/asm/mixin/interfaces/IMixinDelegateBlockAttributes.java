/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.almura.content.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.loader.CatalogDelegate;

public interface IMixinDelegateBlockAttributes {

    void setSoundGroupDelegate(final CatalogDelegate<BlockSoundGroup> delegate);
}
