/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.toolbox.config.processor.ConfigProcessor;

public interface GenerationContentProcessor<C extends ContentGenerator, B extends ContentGenerator.Builder<C>> extends ConfigProcessor<B> {
}
