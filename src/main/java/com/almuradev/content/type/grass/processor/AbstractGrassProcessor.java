/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.grass.processor;

import com.almuradev.content.type.grass.Grass;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

interface AbstractGrassProcessor extends TaggedConfigProcessor<Grass.Builder, ConfigTag> {
}
