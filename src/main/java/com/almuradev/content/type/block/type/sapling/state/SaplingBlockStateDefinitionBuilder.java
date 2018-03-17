/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.sapling.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.tree.Tree;

public interface SaplingBlockStateDefinitionBuilder extends BlockStateDefinition.Builder<SaplingBlockStateDefinition> {
    void tree(final Delegate<Tree> tree);
}
