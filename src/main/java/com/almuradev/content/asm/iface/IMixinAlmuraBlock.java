/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.asm.iface;

import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;

import javax.annotation.Nullable;

public interface IMixinAlmuraBlock {

    @Nullable
    BlockDestroyAction destroyAction();

    void destroyAction(final BlockDestroyAction destroyAction);
}
