/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.tree.processor;

import com.almuradev.content.type.generation.GenerationContentProcessor;
import com.almuradev.content.type.generation.type.feature.tree.TreeGenerator;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

public interface AbstractTreeProcessor extends GenerationContentProcessor<TreeGenerator, TreeGenerator.Builder>, TaggedConfigProcessor<TreeGenerator.Builder, ConfigTag> {
}
