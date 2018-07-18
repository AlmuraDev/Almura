/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.processor;

import com.almuradev.content.component.delegate.DelegateSet;
import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.item.type.tool.ToolItem;
import com.almuradev.content.type.item.type.tool.ToolItemConfig;
import com.almuradev.content.type.item.type.tool.ToolItemProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;
import net.minecraft.block.Block;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import org.spongepowered.api.block.BlockType;

import java.util.stream.Collectors;

public abstract class EffectiveOnProcessor implements ToolItemProcessor.AnyTagged<ToolItem, ToolItem.Builder<ToolItem>> {
    private static final ConfigTag TAG = ConfigTag.create(ToolItemConfig.EFFECTIVE_ON);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public abstract boolean required();

    @Override
    public void processTagged(final ConfigurationNode config, final ToolItem.Builder builder) {
        builder.effectiveOn(new DelegateSet<>(Block.class, config.getList(Types::asString).stream().map(block -> CatalogDelegate.namespaced(BlockType.class, block)).collect(Collectors.toSet())));
    }

    public static final class Required extends EffectiveOnProcessor {
        @Override
        public boolean required() {
            return true;
        }
    }
}
