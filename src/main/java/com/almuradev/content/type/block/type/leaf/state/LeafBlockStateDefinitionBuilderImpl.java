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
import com.almuradev.content.type.block.AbstractBlockStateDefinitionBuilder;

public final class LeafBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<LeafBlockStateDefinition, LeafBlockStateDefinitionBuilderImpl> implements LeafBlockStateDefinitionBuilder {
    Delegate<BlockDecayAction> decayAction;

    @Override
    public void decayAction(final Delegate<BlockDecayAction> decayAction) {
        this.decayAction = decayAction;
    }

    @Override
    public LeafBlockStateDefinition build0() {
        return new LeafBlockStateDefinition(this);
    }
}
