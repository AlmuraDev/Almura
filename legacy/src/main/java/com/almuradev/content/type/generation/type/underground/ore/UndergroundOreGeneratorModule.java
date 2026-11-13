/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.underground.ore;

import com.almuradev.content.type.generation.GenerationGenre;
import com.almuradev.content.type.generation.GenerationModule;
import com.almuradev.content.type.generation.type.underground.ore.processor.EntriesOreGenerationContentProcessor;

public final class UndergroundOreGeneratorModule extends GenerationModule.Module {
    @Override
    protected void configure() {
        this.bind(UndergroundOreGenerator.Builder.class).to(UndergroundOreGeneratorBuilder.class);
        this.bind(UndergroundOreDefinition.Builder.class).to(UndergroundOreDefinition.Builder.Impl.class);
        this.processors()
                .only(EntriesOreGenerationContentProcessor.class, GenerationGenre.UNDERGROUND_ORE);
    }
}
