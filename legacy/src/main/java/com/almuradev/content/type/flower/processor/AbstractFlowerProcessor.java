/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.flower.processor;

import com.almuradev.content.type.flower.Flower;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

interface AbstractFlowerProcessor extends TaggedConfigProcessor<Flower.Builder, ConfigTag> {
}
