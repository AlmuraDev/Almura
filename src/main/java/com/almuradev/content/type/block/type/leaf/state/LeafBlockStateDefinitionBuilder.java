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
import com.almuradev.content.type.block.type.leaf.processor.preventdecay.PreventDecay;
import com.almuradev.content.type.block.type.leaf.processor.spread.Spread;

import javax.annotation.Nullable;

public interface LeafBlockStateDefinitionBuilder extends BlockStateDefinition.Builder<LeafBlockStateDefinition> {

    void decayAction(final Delegate<BlockDecayAction> decayAction);

    void spread(@Nullable Spread spread);

    void preventDecay(@Nullable PreventDecay preventDecay);
}
