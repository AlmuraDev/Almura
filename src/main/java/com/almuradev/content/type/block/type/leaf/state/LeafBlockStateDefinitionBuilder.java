/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf.state;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.action.type.blockdecay.BlockDecayAction;
import com.almuradev.content.type.block.BlockStateDefinition;

public interface LeafBlockStateDefinitionBuilder extends BlockStateDefinition.Builder<LeafBlockStateDefinition> {

    void decayAction(final Delegate<BlockDecayAction> decayAction);
}
