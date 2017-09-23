/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.component.sound;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.Constants;
import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.shared.registry.Registries;
import net.minecraft.util.SoundEvent;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.effect.sound.SoundType;

import java.util.Optional;

import javax.annotation.Nullable;

public class BlockSoundGroupBuilder implements BlockSoundGroup.Builder {

    private static final double DEFAULT_VOLUME = 1.0f;
    private static final double DEFAULT_PITCH = 1.0f;
    @Nullable private String id;
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
        if (node.getNode(BlockConfig.State.Sound.PARENT).isVirtual()) {
            parent = Optional.empty();
        } else {
            parent = Registries.findCatalog(BlockSoundGroup.class, node.getNode(BlockConfig.State.Sound.PARENT).getString());
        }
        findSound(node.getNode(BlockConfig.State.Sound.SOUNDS, BlockConfig.State.Sound.Sounds.BREAK)).ifPresent(builder::breakSound);
        findSound(node.getNode(BlockConfig.State.Sound.SOUNDS, BlockConfig.State.Sound.Sounds.STEP)).ifPresent(builder::stepSound);
        findSound(node.getNode(BlockConfig.State.Sound.SOUNDS, BlockConfig.State.Sound.Sounds.PLACE)).ifPresent(builder::placeSound);
        findSound(node.getNode(BlockConfig.State.Sound.SOUNDS, BlockConfig.State.Sound.Sounds.HIT)).ifPresent(builder::hitSound);
        findSound(node.getNode(BlockConfig.State.Sound.SOUNDS, BlockConfig.State.Sound.Sounds.FALL)).ifPresent(builder::fallSound);
        if (!builder.populated(PopulationCheckType.SOUNDS)) {
            if (!parent.isPresent()) {
                throw new IllegalStateException("Unable to find a parent sound group to build a complete sound group for " + id);
            }
            builder.softFrom(parent.get());
        }
        if (!node.getNode(BlockConfig.State.Sound.VOLUME).isVirtual()) {
            builder.volume(node.getNode(BlockConfig.State.Sound.VOLUME).getDouble());
        }
        if (!node.getNode(BlockConfig.State.Sound.PITCH).isVirtual()) {
            builder.volume(node.getNode(BlockConfig.State.Sound.PITCH).getDouble());
        }
        if (!builder.populated(PopulationCheckType.VOLUME_PITCH)) {
            builder.volume(DEFAULT_VOLUME).pitch(DEFAULT_PITCH);
        }
    }

    public static BlockSoundGroup createVirtual(final String id, final ConfigurationNode node) {
        final BlockSoundGroup.Builder builder = BlockSoundGroup.builder();
        fill(builder, id, node);
        return builder.build();
    }

    private static Optional<SoundType> findSound(final ConfigurationNode node) {
        return node.isVirtual() ? Optional.empty() : Registries.findCatalog(SoundType.class, node.getString().replace("_", "."));
    }

    @Override
    public String id() {
        return checkNotNull(this.id, "id");
    }

    @Override
    public BlockSoundGroup.Builder id(final String id) {
        this.id = id;
        return this;
    }

    @Override
    public BlockSoundGroup.Builder volume(final double volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public BlockSoundGroup.Builder pitch(final double pitch) {
        this.pitch = pitch;
        return this;
    }

    @Override
    public BlockSoundGroup.Builder breakSound(final SoundType sound) {
        this.breakSound = sound;
        return this;
    }

    @Override
    public BlockSoundGroup.Builder stepSound(final SoundType sound) {
        this.stepSound = checkNotNull(sound, "fall sound");
        return this;
    }

    @Override
    public BlockSoundGroup.Builder placeSound(final SoundType sound) {
        this.placeSound = checkNotNull(sound, "place sound");
        return this;
    }

    @Override
    public BlockSoundGroup.Builder hitSound(final SoundType sound) {
        this.hitSound = checkNotNull(sound, "hit sound");
        return this;
    }

    @Override
    public BlockSoundGroup.Builder fallSound(final SoundType sound) {
        this.fallSound = checkNotNull(sound, "fall sound");
        return this;
    }

    @Override
    public boolean populated(final PopulationCheckType type) {
        if (type == PopulationCheckType.SOUNDS) {
            return this.breakSound != null
                    && this.stepSound != null
                    && this.placeSound != null
                    && this.hitSound != null
                    && this.fallSound != null;
        }
        return this.volume != null
                && this.pitch != null;
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
        this.id = null;
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
    public BlockSoundGroup build() {
        checkState(this.id != null, "id must be set");
        return this.build(Constants.Plugin.ID + ':' + this.id, this.id);
    }

    @Override
    public BlockSoundGroup build(final String id, final String name) {
        checkState(this.id != null, "id must be set");
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
