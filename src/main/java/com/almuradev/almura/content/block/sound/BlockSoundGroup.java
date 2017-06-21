/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.sound;

import com.almuradev.almura.registry.BuildableCatalogType;
import org.spongepowered.api.effect.sound.SoundType;

// Extension of SpongeAPI BlockSoundGroup to make it a catalog type
public interface BlockSoundGroup extends org.spongepowered.api.block.BlockSoundGroup, BuildableCatalogType {

    interface Builder extends BuildableCatalogType.Builder<BlockSoundGroup, Builder> {

        double volume();

        Builder volume(double volume);

        double pitch();

        Builder pitch(double pitch);

        SoundType breakSound();

        Builder breakSound(SoundType sound);

        SoundType stepSound();

        Builder stepSound(SoundType sound);

        SoundType placeSound();

        Builder placeSound(SoundType sound);

        SoundType hitSound();

        Builder hitSound(SoundType sound);

        SoundType fallSound();

        Builder fallSound(SoundType sound);
    }
}
