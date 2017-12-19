/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdestroy.processor;

import com.almuradev.content.component.apply.ApplyParser;
import com.almuradev.content.type.action.ActionContentProcessor;
import com.almuradev.content.type.action.component.drop.DropParser;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyActionConfig;
import com.almuradev.content.type.item.definition.ItemDefinition;
import com.almuradev.toolbox.config.processor.AbstractArrayConfigProcessor;
import com.almuradev.toolbox.config.processor.ArrayConfigProcessor;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class BlockDestroyActionContentProcessor implements ArrayConfigProcessor<BlockDestroyAction.Builder>, AbstractArrayConfigProcessor.EqualTreatment<BlockDestroyAction.Builder>, ActionContentProcessor<BlockDestroyAction, BlockDestroyAction.Builder> {

    private final ApplyParser action;
    private final DropParser drop;

    @Inject
    private BlockDestroyActionContentProcessor(final ApplyParser action, final DropParser drop) {
        this.action = action;
        this.drop = drop;
    }

    @Override
    public void processChild(final ConfigurationNode config, final int index, final BlockDestroyAction.Builder builder) {
        final BlockDestroyAction.Entry.Builder entry = builder.entry(index);
        entry.apply(this.action.parse(config.getNode(BlockDestroyActionConfig.APPLY)));
        entry.drop(this.drop.parse(config.getNode(BlockDestroyActionConfig.DROP)));
        entry.with(this.with(config.getNode(BlockDestroyActionConfig.WITH)));
    }

    private List<ItemDefinition> with(final ConfigurationNode config) {
        final Object value = config.getValue();
        if (value instanceof String || value instanceof ConfigurationNode) {
            return ItemDefinition.PARSER.deserialize(config)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }
        return config.getChildrenList().stream()
                .map(ItemDefinition.PARSER::deserialize)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
