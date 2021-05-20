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
import com.almuradev.content.type.block.type.leaf.processor.preventdecay.PreventDecay;
import com.almuradev.content.type.block.type.leaf.processor.spread.Spread;

import javax.annotation.Nullable;

public final class LeafBlockStateDefinitionBuilderImpl extends AbstractBlockStateDefinitionBuilder<LeafBlockStateDefinition, LeafBlockStateDefinitionBuilderImpl> implements LeafBlockStateDefinitionBuilder {
    Delegate<BlockDecayAction> decayAction;
    @Nullable Spread spread;
    @Nullable PreventDecay preventDecay;

    @Override
    public void decayAction(final Delegate<BlockDecayAction> decayAction) {
        this.decayAction = decayAction;
    }

    @Override
    public void spread(@Nullable final Spread spread) {
        this.spread = spread;
    }

    @Override
    public void preventDecay(@Nullable final PreventDecay preventDecay) {
        this.preventDecay = preventDecay;
    }

    @Override
    public LeafBlockStateDefinition build0() {
        return new LeafBlockStateDefinition(this);
    }
}
