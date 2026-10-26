/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor;

import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.content.type.block.type.crop.CropBlockConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

/**
 * {@code "aquatic": true}: the crop grows underwater. It's planted on the seabed (sand, gravel, dirt or clay) into still water, needs water
 * above it to stay and grow, and turns back into water when it's gone.
 */
public final class AquaticProcessor implements CropBlockContentProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(CropBlockConfig.AQUATIC);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final CropBlock.Builder builder) {
        builder.aquatic(config.getBoolean(false));
    }
}
