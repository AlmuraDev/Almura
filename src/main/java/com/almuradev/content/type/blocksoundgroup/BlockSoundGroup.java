/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import org.spongepowered.api.effect.sound.SoundType;

public interface BlockSoundGroup extends org.spongepowered.api.block.BlockSoundGroup, CatalogedContent {

    interface Builder extends ContentBuilder<BlockSoundGroup> {

        void volume(final double volume);

        void pitch(final double pitch);

        void breakSound(final SoundType sound);

        void stepSound(final SoundType sound);

        void placeSound(final SoundType sound);

        void hitSound(final SoundType sound);

        void fallSound(final SoundType sound);

        void inherit(final Builder that);
    }
}
