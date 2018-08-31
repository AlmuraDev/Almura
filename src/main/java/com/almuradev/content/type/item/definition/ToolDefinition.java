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

        // tool_class:
        //   level: <value>
        if (node.getValue() instanceof Map) {
            final Map.Entry<Object, ? extends ConfigurationNode> entry = node.getChildrenMap().entrySet().iterator().next();
            final String toolClass = String.valueOf(entry.getKey());
            final int level = entry.getValue().getNode(ToolDefinitionConfig.LEVEL).getInt(0);
            return Optional.of(new ToolDefinition(toolClass, level));
        }

        final String toolClass = node.getNode(ToolDefinitionConfig.TOOL_CLASS).getString("");
        final int level = node.getNode(ToolDefinitionConfig.LEVEL).getInt(0);

        return Optional.of(new ToolDefinition(toolClass, level));
    };

    private final String toolClass;
    private final int level;

    ToolDefinition(final String toolClass, final int level) {
        this.level = level;
        this.toolClass = toolClass;
    }

    @Override
    public boolean test(final ItemStack stack) {
        return stack.getItem().getHarvestLevel(stack, this.toolClass, null, null) >= this.level;
    }
}
