/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.action.type.blockdecay.BlockDecayAction;
import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.content.type.block.mixin.iface.IMixinDecayBlock;
import com.almuradev.content.type.block.type.leaf.LeafBlockImpl;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinition;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(value = LeafBlockImpl.class, priority = 1002)
public abstract class MixinDecayBlock implements IMixinDecayBlock, ContentBlock {

    @Nullable
    @Override
    public BlockDecayAction decayAction(final IBlockState state) {
        return Delegate.get(((LeafBlockStateDefinition) this.definition(state)).decayAction);
    }
}
