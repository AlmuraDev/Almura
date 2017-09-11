/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.sound;

import com.almuradev.almura.registry.BuildableCatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.util.annotation.CatalogedBy;

// Extension of SpongeAPI BlockSoundGroup to make it a catalog type
@CatalogedBy(BlockSoundGroups.class)
public interface BlockSoundGroup extends org.spongepowered.api.block.BlockSoundGroup, BuildableCatalogType {

    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder extends BuildableCatalogType.Builder<BlockSoundGroup, Builder> {

        String id();

        Builder id(final String id);

        Builder volume(final double volume);

        Builder pitch(final double pitch);

        Builder breakSound(final SoundType sound);

        Builder stepSound(final SoundType sound);

        Builder placeSound(final SoundType sound);

        Builder hitSound(final SoundType sound);

        Builder fallSound(final SoundType sound);

        boolean populated(final PopulationCheckType type);

        Builder softFrom(final BlockSoundGroup group);

        BlockSoundGroup build();

        /**
         * @deprecated use {@link #build()}, these parameters are ignored
         */
        @Deprecated
        @Override
        BlockSoundGroup build(String id, String name);

        enum PopulationCheckType {
            VOLUME_PITCH,
            SOUNDS;
        }
    }
}
