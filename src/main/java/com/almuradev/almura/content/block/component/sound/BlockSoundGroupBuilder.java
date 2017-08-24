/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.component.sound;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.Constants;
import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import net.minecraft.util.SoundEvent;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundType;

import java.util.Optional;

import javax.annotation.Nullable;

public class BlockSoundGroupBuilder implements BlockSoundGroup.Builder {

    private static final double DEFAULT_VOLUME = 1.0f;
    private static final double DEFAULT_PITCH = 1.0f;
    private String id;
    @Nullable private Double volume;
    @Nullable private Double pitch;
    @Nullable private SoundType breakSound;
    @Nullable private SoundType stepSound;
    @Nullable private SoundType placeSound;
    @Nullable private SoundType hitSound;
    @Nullable private SoundType fallSound;

    public static void fill(final BlockSoundGroup.Builder builder, final String id, final ConfigurationNode node) {
        builder.id(id);
        final Optional<BlockSoundGroup> parent;
        if (node.getNode(Constants.Config.Block.SoundGroup.PARENT).isVirtual()) {
            parent = Optional.empty();
        } else {
            parent = Sponge.getRegistry().getType(BlockSoundGroup.class, node.getNode(Constants.Config.Block.SoundGroup.PARENT).getString());
        }
        getSound(node.getNode(Constants.Config.Block.SoundGroup.BREAK_SOUND)).ifPresent(builder::breakSound);
        getSound(node.getNode(Constants.Config.Block.SoundGroup.STEP_SOUND)).ifPresent(builder::stepSound);
        getSound(node.getNode(Constants.Config.Block.SoundGroup.PLACE_SOUND)).ifPresent(builder::placeSound);
        getSound(node.getNode(Constants.Config.Block.SoundGroup.HIT_SOUND)).ifPresent(builder::hitSound);
        getSound(node.getNode(Constants.Config.Block.SoundGroup.FALL_SOUND)).ifPresent(builder::fallSound);
        if (!builder.isPopulated(PopulationCheckType.SOUNDS)) {
            if (!parent.isPresent()) {
                throw new IllegalStateException("Unable to find a parent sound group to build a complete sound group for " + id);
            }
            builder.softFrom(parent.get());
        }
        if (!node.getNode(Constants.Config.Block.SoundGroup.VOLUME).isVirtual()) {
            builder.volume(node.getNode(Constants.Config.Block.SoundGroup.VOLUME).getDouble());
        }
        if (!node.getNode(Constants.Config.Block.SoundGroup.PITCH).isVirtual()) {
            builder.volume(node.getNode(Constants.Config.Block.SoundGroup.PITCH).getDouble());
        }
        if (!builder.isPopulated(PopulationCheckType.VOLUME_PITCH)) {
            builder.volume(DEFAULT_VOLUME).pitch(DEFAULT_PITCH);
        }
    }

    private static Optional<SoundType> getSound(final ConfigurationNode node) {
        return node.isVirtual() ? Optional.empty() : Sponge.getRegistry().getType(SoundType.class, node.getString().replace("_", "."));
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public BlockSoundGroup.Builder id(final String id) {
        this.id = id;
        return this;
    }

    @Override
    public double volume() {
        return checkNotNull(this.volume);
    }

    @Override
    public BlockSoundGroup.Builder volume(final double volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public double pitch() {
        return checkNotNull(this.pitch);
    }

    @Override
    public BlockSoundGroup.Builder pitch(final double pitch) {
        this.pitch = pitch;
        return this;
    }

    @Override
    public SoundType breakSound() {
        return this.breakSound;
    }

    @Override
    public BlockSoundGroup.Builder breakSound(final SoundType sound) {
        this.breakSound = sound;
        return this;
    }

    @Override
    public SoundType stepSound() {
        return this.stepSound;
    }

    @Override
    public BlockSoundGroup.Builder stepSound(final SoundType sound) {
        this.stepSound = checkNotNull(sound, "fall sound");
        return this;
    }

    @Override
    public SoundType placeSound() {
        return this.placeSound;
    }

    @Override
    public BlockSoundGroup.Builder placeSound(final SoundType sound) {
        this.placeSound = checkNotNull(sound, "place sound");
        return this;
    }

    @Override
    public SoundType hitSound() {
        return this.hitSound;
    }

    @Override
    public BlockSoundGroup.Builder hitSound(final SoundType sound) {
        this.hitSound = checkNotNull(sound, "hit sound");
        return this;
    }

    @Override
    public SoundType fallSound() {
        return this.fallSound;
    }

    @Override
    public BlockSoundGroup.Builder fallSound(final SoundType sound) {
        this.fallSound = checkNotNull(sound, "fall sound");
        return this;
    }

    @Override
    public boolean isPopulated(final PopulationCheckType type) {
        return type == PopulationCheckType.SOUNDS ? (this.breakSound != null && this.stepSound != null && this.placeSound != null && this.hitSound != null && this.fallSound != null) : (this.volume != null && this.pitch != null);
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
    public BlockSoundGroup.Builder softFrom(final BlockSoundGroup value) {
        if (this.volume == null) {
            this.volume = value.getVolume();
        }
        if (this.pitch == null) {
            this.pitch = value.getPitch();
        }
        if (this.breakSound == null) {
            this.breakSound = value.getBreakSound();
        }
        if (this.stepSound == null) {
            this.stepSound = value.getStepSound();
        }
        if (this.placeSound == null) {
            this.placeSound = value.getPlaceSound();
        }
        if (this.hitSound == null) {
            this.hitSound = value.getHitSound();
        }
        if (this.fallSound == null) {
            this.fallSound = value.getFallSound();
        }
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
    public BlockSoundGroup build(final String id, final String name) {
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
