/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;
import com.almuradev.content.type.tree.Tree;

public final class SaplingBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<SaplingBlockStateDefinition, SaplingBlockStateDefinitionBuilderImpl> implements SaplingBlockStateDefinitionBuilder {
    Delegate<Tree> tree;

    @Override
    public void tree(final Delegate<Tree> tree) {
        this.tree = tree;
    }

    @Override
    public SaplingBlockStateDefinition build0() {
        return new SaplingBlockStateDefinition(this);
    }
}
