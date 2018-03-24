/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.deadbush.processor;

import com.almuradev.content.type.generation.GenerationContentProcessor;
import com.almuradev.content.type.generation.type.feature.deadbush.DeadBushGenerator;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

interface AbstractDeadBushProcessor extends GenerationContentProcessor<DeadBushGenerator, DeadBushGenerator.Builder>,
        TaggedConfigProcessor<DeadBushGenerator.Builder, ConfigTag> {

}
