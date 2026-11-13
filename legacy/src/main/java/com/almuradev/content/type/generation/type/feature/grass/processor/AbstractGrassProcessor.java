/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.grass.processor;

import com.almuradev.content.type.generation.GenerationContentProcessor;
import com.almuradev.content.type.generation.type.feature.grass.GrassGenerator;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

interface AbstractGrassProcessor extends GenerationContentProcessor<GrassGenerator, GrassGenerator.Builder>,
        TaggedConfigProcessor<GrassGenerator.Builder, ConfigTag> {

}
