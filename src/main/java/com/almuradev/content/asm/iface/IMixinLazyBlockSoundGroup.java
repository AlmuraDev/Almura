/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.asm.iface;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;

public interface IMixinLazyBlockSoundGroup {

    void soundGroup(final Delegate<BlockSoundGroup> group);
}
