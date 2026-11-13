/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool.type.sickle;

import com.almuradev.content.type.item.type.tool.ToolItem;

public interface SickleToolItem extends ToolItem {
    String TOOL_CLASS = "sickle";

    interface Builder extends ToolItem.Builder<SickleToolItem> {
    }
}
