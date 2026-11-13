/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.flower;

import com.almuradev.content.type.generation.GenerationGenre;
import com.almuradev.content.type.generation.GenerationModule;
import com.almuradev.content.type.generation.type.feature.flower.processor.ChanceProcessor;
import com.almuradev.content.type.generation.type.feature.flower.processor.FlowerProcessor;
import com.almuradev.content.type.generation.type.feature.flower.processor.RequiresProcessor;
import com.almuradev.content.type.generation.type.feature.flower.processor.WorldsProcessor;

public final class FlowerGeneratorModule extends GenerationModule.Module {

    @Override
    protected void configure() {
        this.bind(FlowerGenerator.Builder.class).to(FlowerGeneratorBuilder.class);
        this.processors()
                .only(ChanceProcessor.class, GenerationGenre.FLOWER_FEATURE)
                .only(RequiresProcessor.class, GenerationGenre.FLOWER_FEATURE)
                .only(FlowerProcessor.class, GenerationGenre.FLOWER_FEATURE)
                .only(WorldsProcessor.class, GenerationGenre.FLOWER_FEATURE);
    }
}
