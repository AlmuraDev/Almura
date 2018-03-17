/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed.processor;

import com.almuradev.content.type.item.ItemContentProcessor;
import com.almuradev.content.type.item.type.seed.SeedItem;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface AbstractSeedProcessor extends ItemContentProcessor<SeedItem, SeedItem.Builder> {
    interface AnyTagged extends AbstractSeedProcessor, TaggedConfigProcessor<SeedItem.Builder, ConfigTag> {
    }
}
