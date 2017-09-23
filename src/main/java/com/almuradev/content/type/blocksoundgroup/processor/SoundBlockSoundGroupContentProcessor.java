/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup.processor;

import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroupConfig;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.effect.sound.SoundType;

import java.util.Optional;

import javax.inject.Inject;

public final class SoundBlockSoundGroupContentProcessor implements TaggedConfigProcessor<BlockSoundGroup.Builder, ConfigTag> {

    private static final ConfigTag TAG = ConfigTag.create(BlockSoundGroupConfig.SOUND);
    private final GameRegistry gr;

    @Inject
    private SoundBlockSoundGroupContentProcessor(final GameRegistry gr) {
        this.gr = gr;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final BlockSoundGroup.Builder builder) {
        this.findSound(config.getNode(BlockSoundGroupConfig.Sound.BREAK)).ifPresent(builder::breakSound);
        this.findSound(config.getNode(BlockSoundGroupConfig.Sound.STEP)).ifPresent(builder::stepSound);
        this.findSound(config.getNode(BlockSoundGroupConfig.Sound.PLACE)).ifPresent(builder::placeSound);
        this.findSound(config.getNode(BlockSoundGroupConfig.Sound.HIT)).ifPresent(builder::hitSound);
        this.findSound(config.getNode(BlockSoundGroupConfig.Sound.FALL)).ifPresent(builder::fallSound);
    }

    private Optional<SoundType> findSound(final ConfigurationNode config) {
        if (config.isVirtual()) {
            return Optional.empty();
        }
        return this.gr.getType(SoundType.class, config.getString().replace('_', '.'));
    }
}
