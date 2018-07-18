/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdecay;

import com.almuradev.content.registry.CatalogedContent;

import java.util.List;

public final class BlockDecayActionImpl extends CatalogedContent.Impl implements BlockDecayAction {
    private final List<Entry> entries;

    BlockDecayActionImpl(final String id, final String name, final List<Entry> entries) {
        super(id, name);
        this.entries = entries;
    }

    @Override
    public List<Entry> entries() {
        return this.entries;
    }
}
