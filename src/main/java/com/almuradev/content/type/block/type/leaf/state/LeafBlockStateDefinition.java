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
import com.almuradev.content.type.block.AbstractSingleBlockStateDefinition;
import com.almuradev.content.type.block.component.aabb.BlockAABB;
import com.almuradev.content.type.block.type.leaf.processor.preventdecay.PreventDecay;
import com.almuradev.content.type.block.type.leaf.processor.spread.Spread;

import javax.annotation.Nullable;

public final class LeafBlockStateDefinition extends AbstractSingleBlockStateDefinition<BlockAABB.Box, BlockAABB.Collision, BlockAABB.WireFrame> {
    @Nullable public final Delegate<BlockDecayAction> decayAction;
    @Nullable public final Spread spread;
    @Nullable public final PreventDecay preventDecay;

    LeafBlockStateDefinition(final LeafBlockStateDefinitionBuilderImpl builder) {
        super(builder);
        this.decayAction = builder.decayAction;
        this.spread = builder.spread;
        this.preventDecay = builder.preventDecay;
    }
}
