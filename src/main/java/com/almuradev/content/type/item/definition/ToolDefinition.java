/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.definition;

import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import net.minecraft.item.ItemStack;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Map;
import java.util.Optional;

public final class ToolDefinition implements ItemAcceptable {

    public static final ConfigurationNodeDeserializer<ToolDefinition> PARSER = node -> {
        if (node.isVirtual()) {
            return Optional.empty();
        }

        // tool_type:
        //   level: <value>
        if (node.getValue() instanceof Map) {
            final Map.Entry<Object, ? extends ConfigurationNode> entry = node.getChildrenMap().entrySet().iterator().next();
            final String toolType = String.valueOf(entry.getKey());
            final int level = entry.getValue().getNode(ToolDefinitionConfig.LEVEL).getInt(0);
            return Optional.of(new ToolDefinition(toolType, level));
        }

        final String toolType = node.getNode(ToolDefinitionConfig.TOOL_TYPE).getString("");
        final int level = node.getNode(ToolDefinitionConfig.LEVEL).getInt(0);

        return Optional.of(new ToolDefinition(toolType, level));
    };

    private final String toolType;
    private final int level;

    ToolDefinition(final String toolType, final int level) {
        this.level = level;
        this.toolType = toolType;
    }

    @Override
    public boolean test(final ItemStack stack) {
        return stack.getItem().getHarvestLevel(stack, this.toolType, null, null) >= this.level;
    }
}
