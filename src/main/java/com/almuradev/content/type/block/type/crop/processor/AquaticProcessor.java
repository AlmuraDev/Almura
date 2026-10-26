/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop.processor;

import com.almuradev.content.type.block.type.crop.AquaticMode;
import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.content.type.block.type.crop.CropBlockConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

/**
 * {@code "aquatic"}: the crop grows in water. It's planted on the bed (sand, gravel, dirt, farmland or clay) into still water and turns back
 * into water when it's gone. {@code true} or {@code "submerged"}: fully underwater, needing water above it (seaweed). {@code "surface"}: in a
 * single block of water with air above, drawing its own water surface (rice in a paddy).
 */
public final class AquaticProcessor implements CropBlockContentProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(CropBlockConfig.AQUATIC);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final CropBlock.Builder builder) {
        final String value = config.getString("false").trim().toLowerCase(java.util.Locale.ROOT);
        switch (value) {
            case "true":
            case "submerged":
                builder.aquatic(AquaticMode.SUBMERGED);
                break;
            case "surface":
                builder.aquatic(AquaticMode.SURFACE);
                break;
            case "false":
                builder.aquatic(AquaticMode.NONE);
                break;
            default:
                throw new IllegalArgumentException("Unknown aquatic mode '" + value + "': use true, \"submerged\" or \"surface\"");
        }
    }
}
