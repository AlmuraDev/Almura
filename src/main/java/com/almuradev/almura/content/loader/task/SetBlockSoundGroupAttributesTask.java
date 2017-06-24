/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.task;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Constants;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.loader.AssetContext;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundType;

public class SetBlockSoundGroupAttributesTask implements StageTask<BlockSoundGroup, BlockSoundGroup.Builder> {

    @Override
    public void execute(AssetContext<BlockSoundGroup, BlockSoundGroup.Builder> context) {
        final BlockSoundGroup.Builder builder = context.getBuilder();
        final ConfigurationNode node = context.getAsset().getConfigurationNode();

        builder
                .volume(node.getNode(Constants.Config.Block.SoundGroup.VOLUME).getDouble())
                .pitch(node.getNode(Constants.Config.Block.SoundGroup.PITCH).getDouble())
                .breakSound(this.getSound(node, Constants.Config.Block.SoundGroup.BREAK_SOUND))
                .stepSound(this.getSound(node, Constants.Config.Block.SoundGroup.STEP_SOUND))
                .placeSound(this.getSound(node, Constants.Config.Block.SoundGroup.PLACE_SOUND))
                .hitSound(this.getSound(node, Constants.Config.Block.SoundGroup.HIT_SOUND))
                .fallSound(this.getSound(node, Constants.Config.Block.SoundGroup.FALL_SOUND));
    }

    private SoundType getSound(final ConfigurationNode node, final String key) {
        final String value = checkNotNull(node.getNode(key).getString(null), "missing %s", key).replace("_", ".");
        return checkNotNull(Sponge.getRegistry().getType(SoundType.class, value).orElse(null), "missing sound '%s'", value);
    }
}
