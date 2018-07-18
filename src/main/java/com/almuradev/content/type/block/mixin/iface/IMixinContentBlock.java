/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.iface;

import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;

import java.util.Optional;

import javax.annotation.Nullable;

public interface IMixinContentBlock {
    Optional<BlockSoundGroup> soundGroup(final IBlockState state);

    @Nullable
    BlockDestroyAction destroyAction(final IBlockState state);

    ThreadLocal<EntityPlayer> getHarvesters();

    void setRenderLayer(final BlockRenderLayer renderLayer);
}
