/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.iface;

import com.almuradev.content.type.action.type.blockdecay.BlockDecayAction;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nullable;

public interface IMixinDecayBlock {

    @Nullable
    BlockDecayAction decayAction(final IBlockState state);
}
