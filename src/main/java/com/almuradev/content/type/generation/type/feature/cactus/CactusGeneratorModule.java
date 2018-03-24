/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.cactus;

import com.almuradev.content.type.generation.GenerationGenre;
import com.almuradev.content.type.generation.GenerationModule;
import com.almuradev.content.type.generation.type.feature.cactus.processor.ChanceProcessor;
import com.almuradev.content.type.generation.type.feature.cactus.processor.CactusProcessor;
import com.almuradev.content.type.generation.type.feature.cactus.processor.RequiresProcessor;
import com.almuradev.content.type.generation.type.feature.cactus.processor.WorldsProcessor;

public final class CactusGeneratorModule extends GenerationModule.Module {

    @Override
    protected void configure() {
        this.bind(CactusGenerator.Builder.class).to(CactusGeneratorBuilder.class);
        this.processors()
                .only(ChanceProcessor.class, GenerationGenre.CACTUS_FEATURE)
                .only(RequiresProcessor.class, GenerationGenre.CACTUS_FEATURE)
                .only(CactusProcessor.class, GenerationGenre.CACTUS_FEATURE)
                .only(WorldsProcessor.class, GenerationGenre.CACTUS_FEATURE);
    }
}
