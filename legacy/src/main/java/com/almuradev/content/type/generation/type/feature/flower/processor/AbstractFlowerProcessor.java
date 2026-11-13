/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.flower.processor;

import com.almuradev.content.type.generation.GenerationContentProcessor;
import com.almuradev.content.type.generation.type.feature.flower.FlowerGenerator;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

interface AbstractFlowerProcessor extends GenerationContentProcessor<FlowerGenerator, FlowerGenerator.Builder>,
        TaggedConfigProcessor<FlowerGenerator.Builder, ConfigTag> {

}
