/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.sound;

import com.almuradev.almura.Constants;
import com.almuradev.almura.registry.BuildableCatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundType;

// Extension of SpongeAPI BlockSoundGroup to make it a catalog type
public interface BlockSoundGroup extends org.spongepowered.api.block.BlockSoundGroup, BuildableCatalogType {

    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder extends BuildableCatalogType.Builder<BlockSoundGroup, Builder> {

        String id();

        Builder id(final String id);

        double volume();

        Builder volume(final double volume);

        double pitch();

        Builder pitch(final double pitch);

        SoundType breakSound();

        Builder breakSound(final SoundType sound);

        SoundType stepSound();

        Builder stepSound(final SoundType sound);

        SoundType placeSound();

        Builder placeSound(final SoundType sound);

        SoundType hitSound();

        Builder hitSound(final SoundType sound);

        SoundType fallSound();

        Builder fallSound(final SoundType sound);

        boolean isPopulated(final PopulationCheckType type);

        Builder softFrom(final BlockSoundGroup group);

        default BlockSoundGroup build() {
            return this.build(Constants.Plugin.ID + ':' + this.id(), this.id());
        }

        enum PopulationCheckType {
            VOLUME_PITCH,
            SOUNDS;
        }
    }
}
