/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.underground.ore.processor;

import com.almuradev.content.type.generation.GenerationContentProcessor;
import com.almuradev.content.type.generation.type.underground.ore.UndergroundOreGenerator;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface OreGenerationContentProcessor extends GenerationContentProcessor<UndergroundOreGenerator, UndergroundOreGenerator.Builder>, TaggedConfigProcessor<UndergroundOreGenerator.Builder, ConfigTag> {
}
