/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.processor;

import com.almuradev.content.component.delegate.DelegateSet;
import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlock;
import com.almuradev.toolbox.config.tag.ConfigTag;
import net.minecraft.item.Item;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import org.spongepowered.api.item.ItemType;

import java.util.stream.Collectors;

public final class EffectiveToolsProcessor implements BlockContentProcessor.AnyTagged {
    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.EFFECTIVE_TOOLS);

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public void processTagged(final ConfigurationNode config, final ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder) {
        builder.effectiveTools(new DelegateSet<>(Item.class, config.getList(Types::asString).stream().map(item -> CatalogDelegate.namespaced(ItemType.class, item)).collect(Collectors.toSet())));
    }
}
