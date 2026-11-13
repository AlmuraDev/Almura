/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.definition;

public interface ToolDefinitionConfig {
    /**
     * Forge refers to this as "tool class". E.g.
     * {@link net.minecraft.item.Item#getToolClasses(net.minecraft.item.ItemStack)}
     */
    String TOOL_TYPE = "tool_type";
    String LEVEL = "level";
}
