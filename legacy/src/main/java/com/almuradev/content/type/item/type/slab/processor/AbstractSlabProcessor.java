/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.slab.processor;

import com.almuradev.content.type.item.ItemContentProcessor;
import com.almuradev.content.type.item.type.slab.SlabItem;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface AbstractSlabProcessor extends ItemContentProcessor<SlabItem, SlabItem.Builder> {
    interface AnyTagged extends AbstractSlabProcessor, TaggedConfigProcessor<SlabItem.Builder, ConfigTag> {
    }
}
