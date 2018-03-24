/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.cactus.processor;

import com.almuradev.content.type.generation.GenerationContentProcessor;
import com.almuradev.content.type.generation.type.feature.cactus.CactusGenerator;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

interface AbstractCactusProcessor extends GenerationContentProcessor<CactusGenerator, CactusGenerator.Builder>,
        TaggedConfigProcessor<CactusGenerator.Builder, ConfigTag> {

}
