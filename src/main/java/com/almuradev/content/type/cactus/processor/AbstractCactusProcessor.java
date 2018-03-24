/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.cactus.processor;

import com.almuradev.content.type.cactus.Cactus;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

interface AbstractCactusProcessor extends TaggedConfigProcessor<Cactus.Builder, ConfigTag> {
}
