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

public interface ItemContentProcessor<C extends ContentItemType, B extends ContentItemType.Builder<C>> extends ConfigProcessor<B> {

    interface AnyTagged extends ItemContentProcessor<ContentItemType, ContentItemType.Builder<ContentItemType>>, TaggedConfigProcessor<ContentItemType.Builder<ContentItemType>, ConfigTag> {

    }
}
