/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.shared.registry.catalog.CatalogDelegate;

public interface IMixinDelegateBlockAttributes {

    void setSoundGroupDelegate(final CatalogDelegate<BlockSoundGroup> delegate);
}
