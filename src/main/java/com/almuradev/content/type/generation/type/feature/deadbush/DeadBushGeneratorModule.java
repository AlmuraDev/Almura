/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.deadbush;

import com.almuradev.content.type.generation.GenerationGenre;
import com.almuradev.content.type.generation.GenerationModule;
import com.almuradev.content.type.generation.type.feature.deadbush.processor.ChanceProcessor;
import com.almuradev.content.type.generation.type.feature.deadbush.processor.DeadProcessor;
import com.almuradev.content.type.generation.type.feature.deadbush.processor.RequiresProcessor;
import com.almuradev.content.type.generation.type.feature.deadbush.processor.WorldsProcessor;

public final class DeadBushGeneratorModule extends GenerationModule.Module {

    @Override
    protected void configure() {
        this.bind(DeadBushGenerator.Builder.class).to(DeadBushGeneratorBuilder.class);
        this.processors()
                .only(ChanceProcessor.class, GenerationGenre.DEAD_BUSH_FEATURE)
                .only(RequiresProcessor.class, GenerationGenre.DEAD_BUSH_FEATURE)
                .only(DeadProcessor.class, GenerationGenre.DEAD_BUSH_FEATURE)
                .only(WorldsProcessor.class, GenerationGenre.DEAD_BUSH_FEATURE);
    }
}
