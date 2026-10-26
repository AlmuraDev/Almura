/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree.processor;

import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.type.tree.TreeConfig;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

/**
 * {@code "palm": true}: the tree grows as a palm (a tall leaning trunk, a crown of drooping fronds, fruit under the crown).
 */
public final class PalmProcessor implements AbstractTreeProcessor {
    private static final ConfigTag TAG = ConfigTag.create(TreeConfig.PALM);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final Tree.Builder builder) {
        builder.palm(config.getBoolean());
    }
}
