/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.processor;

import com.almuradev.content.type.item.ContentItem;
import com.almuradev.content.type.item.ItemConfig;
import com.almuradev.content.type.item.ItemContainer;
import com.almuradev.content.type.item.ItemContentProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import net.minecraft.util.ResourceLocation;
import ninja.leaping.configurate.ConfigurationNode;

public final class ContainerProcessor implements ItemContentProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(ItemConfig.CONTAINER);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final ContentItem.Builder builder) {
        if (config.hasMapChildren()) {
            final ConfigurationNode item = config.getNode(ItemConfig.Container.ITEM);
            if (!item.isVirtual()) {
                builder.container(ItemContainer.of(new ResourceLocation(item.getString())));
            } else {
                builder.container(ItemContainer.damaged(config.getNode(ItemConfig.Container.DAMAGE).getInt(1)));
            }
        } else {
            builder.container(ItemContainer.of(new ResourceLocation(config.getString())));
        }
    }
}
