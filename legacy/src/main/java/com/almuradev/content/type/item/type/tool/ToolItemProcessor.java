/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool;

import com.almuradev.content.type.item.ItemContentProcessor;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface ToolItemProcessor<T extends ToolItem, B extends ToolItem.Builder<T>> extends ItemContentProcessor<T, B> {
    interface AnyTagged<T extends ToolItem, B extends ToolItem.Builder<T>> extends ToolItemProcessor<T, B>, TaggedConfigProcessor<B, ConfigTag> {
    }
}
