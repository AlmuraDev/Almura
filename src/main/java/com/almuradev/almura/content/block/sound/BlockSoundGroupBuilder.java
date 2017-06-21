/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.sound;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import net.minecraft.util.SoundEvent;
import org.spongepowered.api.effect.sound.SoundType;

import javax.annotation.Nullable;

public class BlockSoundGroupBuilder implements BlockSoundGroup.Builder {

    @Nullable private Double volume;
    @Nullable private Double pitch;
    @Nullable private SoundType breakSound;
    @Nullable private SoundType stepSound;
    @Nullable private SoundType placeSound;
    @Nullable private SoundType hitSound;
    @Nullable private SoundType fallSound;

    @Override
    public double volume() {
        return checkNotNull(this.volume);
    }

    @Override
    public BlockSoundGroup.Builder volume(double volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public double pitch() {
        return checkNotNull(this.pitch);
    }

    @Override
    public BlockSoundGroup.Builder pitch(double pitch) {
        this.pitch = pitch;
        return this;
    }

    @Override
    public SoundType breakSound() {
        return this.breakSound;
    }

    @Override
    public BlockSoundGroup.Builder breakSound(SoundType sound) {
        this.breakSound = sound;
        return this;
    }

    @Override
    public SoundType stepSound() {
        return this.stepSound;
    }

    @Override
    public BlockSoundGroup.Builder stepSound(SoundType sound) {
        this.stepSound = checkNotNull(sound, "fall sound");
        return this;
    }

    @Override
    public SoundType placeSound() {
        return this.placeSound;
    }

    @Override
    public BlockSoundGroup.Builder placeSound(SoundType sound) {
        this.placeSound = checkNotNull(sound, "place sound");
        return this;
    }

    @Override
    public SoundType hitSound() {
        return this.hitSound;
    }

    @Override
    public BlockSoundGroup.Builder hitSound(SoundType sound) {
        this.hitSound = checkNotNull(sound, "hit sound");
        return this;
    }

    @Override
    public SoundType fallSound() {
        return this.fallSound;
    }

    @Override
    public BlockSoundGroup.Builder fallSound(SoundType sound) {
        this.fallSound = checkNotNull(sound, "fall sound");
        return this;
    }

    @Override
    public BlockSoundGroup.Builder from(BlockSoundGroup value) {
        this.volume = value.getVolume();
        this.pitch = value.getPitch();
        this.breakSound = value.getBreakSound();
        this.stepSound = value.getStepSound();
        this.placeSound = value.getPlaceSound();
        this.hitSound = value.getHitSound();
        this.fallSound = value.getFallSound();
        return this;
    }

    @Override
    public BlockSoundGroup.Builder reset() {
        this.volume = null;
        this.pitch = null;
        this.breakSound = null;
        this.stepSound = null;
        this.placeSound = null;
        this.hitSound = null;
        this.fallSound = null;
        return this;
    }

    @Override
    public BlockSoundGroup build(String id, String name) {
        checkState(this.volume != null, "volume must be set");
        checkState(this.pitch != null, "pitch must be set");
        checkState(this.breakSound != null, "break sound must be set");
        checkState(this.stepSound != null, "step sound must be set");
        checkState(this.placeSound != null, "place sound must be set");
        checkState(this.hitSound != null, "hit sound must be set");
        checkState(this.fallSound != null, "fall sound must be set");
        final BlockSoundGroup group = (BlockSoundGroup) new net.minecraft.block.SoundType(this.volume.floatValue(), this.pitch.floatValue(), (SoundEvent) this.breakSound, (SoundEvent) this.stepSound, (SoundEvent) this.placeSound, (SoundEvent) this.hitSound, (SoundEvent) this.fallSound);
        ((IMixinSetCatalogTypeId) group).setId(id, name);
        return group;
    }
}
