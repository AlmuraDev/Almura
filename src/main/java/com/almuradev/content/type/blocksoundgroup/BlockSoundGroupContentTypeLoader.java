/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup;

import com.almuradev.almura.shared.event.Witness;
import com.almuradev.content.loader.SingleTypeContentLoader;
import com.almuradev.content.loader.SingleTypeExternalContentProcessor;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.inject.Singleton;

@Singleton
public final class BlockSoundGroupContentTypeLoader extends SingleTypeContentLoader<BlockSoundGroup, BlockSoundGroup.Builder> implements SingleTypeExternalContentProcessor<BlockSoundGroup, BlockSoundGroup.Builder>, Witness {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void blocks(final RegistryEvent.Register<Block> event) {
        this.build();
    }
}
