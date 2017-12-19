/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdestroy;

import com.almuradev.content.registry.CatalogedContent;

import java.util.List;

public final class BlockDestroyActionImpl extends CatalogedContent.Impl implements BlockDestroyAction {

    private final List<Entry> entries;

    BlockDestroyActionImpl(final String id, final String name, final List<Entry> entries) {
        super(id, name);
        this.entries = entries;
    }

    @Override
    public List<Entry> entries() {
        return this.entries;
    }
}
