/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.registry.ContentBuilder;
import net.minecraft.util.SoundEvent;
import org.spongepowered.api.effect.sound.SoundType;

import javax.annotation.Nullable;

final class BlockSoundGroupBuilder extends ContentBuilder.Impl<BlockSoundGroup> implements BlockSoundGroup.Builder {
    @Nullable private Double volume;
    @Nullable private Double pitch;
    @Nullable private SoundType breakSound;
    @Nullable private SoundType stepSound;
    @Nullable private SoundType placeSound;
    @Nullable private SoundType hitSound;
    @Nullable private SoundType fallSound;

    @Override
    public void volume(final double volume) {
        this.volume = volume;
    }

    @Override
    public void pitch(final double pitch) {
        this.pitch = pitch;
    }

    @Override
    public void breakSound(final SoundType sound) {
        this.breakSound = sound;
    }

    @Override
    public void stepSound(final SoundType sound) {
        this.stepSound = checkNotNull(sound, "fall sound");
    }

    @Override
    public void placeSound(final SoundType sound) {
        this.placeSound = checkNotNull(sound, "place sound");
    }

    @Override
    public void hitSound(final SoundType sound) {
        this.hitSound = checkNotNull(sound, "hit sound");
    }

    @Override
    public void fallSound(final SoundType sound) {
        this.fallSound = checkNotNull(sound, "fall sound");
    }

    @Override
    public void inherit(final BlockSoundGroup.Builder that) {
        this.inherit((BlockSoundGroupBuilder) that);
    }

    private void inherit(final BlockSoundGroupBuilder that) {
        if (this.volume == null) {
            this.volume = that.volume;
        }
        if (this.pitch == null) {
            this.pitch = that.pitch;
        }
        if (this.breakSound == null) {
            this.breakSound = that.breakSound;
        }
        if (this.stepSound == null) {
            this.stepSound = that.stepSound;
        }
        if (this.placeSound == null) {
            this.placeSound = that.placeSound;
        }
        if (this.hitSound == null) {
            this.hitSound = that.hitSound;
        }
        if (this.fallSound == null) {
            this.fallSound = that.fallSound;
        }
    }

    @Override
    public BlockSoundGroup build() {
        checkState(this.volume != null, "volume must be set");
        checkState(this.pitch != null, "pitch must be set");
        checkState(this.breakSound != null, "break sound must be set");
        checkState(this.stepSound != null, "step sound must be set");
        checkState(this.placeSound != null, "place sound must be set");
        checkState(this.hitSound != null, "hit sound must be set");
        checkState(this.fallSound != null, "fall sound must be set");
        final BlockSoundGroup group = (BlockSoundGroup) new net.minecraft.block.SoundType(
                this.volume.floatValue(),
                this.pitch.floatValue(),
                (SoundEvent) this.breakSound,
                (SoundEvent) this.stepSound,
                (SoundEvent) this.placeSound,
                (SoundEvent) this.hitSound,
                (SoundEvent) this.fallSound
        );
        ((IMixinSetCatalogTypeId) group).setId(this.id, this.name);
        return group;
    }
}
