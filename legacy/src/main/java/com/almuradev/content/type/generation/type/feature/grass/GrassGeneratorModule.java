/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.grass;

import com.almuradev.content.type.generation.GenerationGenre;
import com.almuradev.content.type.generation.GenerationModule;
import com.almuradev.content.type.generation.type.feature.grass.processor.ChanceProcessor;
import com.almuradev.content.type.generation.type.feature.grass.processor.GrassProcessor;
import com.almuradev.content.type.generation.type.feature.grass.processor.RequiresProcessor;
import com.almuradev.content.type.generation.type.feature.grass.processor.WorldsProcessor;

public final class GrassGeneratorModule extends GenerationModule.Module {

    @Override
    protected void configure() {
        this.bind(GrassGenerator.Builder.class).to(GrassGeneratorBuilder.class);
        this.processors()
                .only(ChanceProcessor.class, GenerationGenre.GRASS_FEATURE)
                .only(RequiresProcessor.class, GenerationGenre.GRASS_FEATURE)
                .only(GrassProcessor.class, GenerationGenre.GRASS_FEATURE)
                .only(WorldsProcessor.class, GenerationGenre.GRASS_FEATURE);
    }
}
