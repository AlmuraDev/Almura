/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore;

import com.almuradev.content.type.generation.GenerationGenre;
import com.almuradev.content.type.generation.GenerationModule;
import com.almuradev.content.type.generation.type.ore.processor.EntriesOreGenerationContentProcessor;
import com.almuradev.content.type.generation.type.ore.processor.WeightOreGenerationContentProcessor;

public final class OreGeneratorModule extends GenerationModule.Module {
    @Override
    protected void configure() {
        this.bind(OreGenerator.Builder.class).to(OreGeneratorBuilder.class);
        this.bind(OreDefinition.Builder.class).to(OreDefinition.Builder.Impl.class);
        this.processors()
                .only(EntriesOreGenerationContentProcessor.class, GenerationGenre.ORE)
                .only(WeightOreGenerationContentProcessor.class, GenerationGenre.ORE);
    }
}
