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

public final class BigProcessor implements AbstractTreeProcessor {
    private static final ConfigTag TAG = ConfigTag.create(TreeConfig.BIG);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final Tree.Builder builder) {
        builder.big(config.getBoolean());
    }
}
