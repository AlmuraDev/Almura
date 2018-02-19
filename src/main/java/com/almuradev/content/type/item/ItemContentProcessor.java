/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface ItemContentProcessor<C extends ContentItem, B extends ContentItem.Builder<C>> extends ConfigProcessor<B> {
    interface AnyTagged extends ItemContentProcessor<ContentItem, ContentItem.Builder<ContentItem>>, TaggedConfigProcessor<ContentItem.Builder<ContentItem>, ConfigTag> {
    }
}
