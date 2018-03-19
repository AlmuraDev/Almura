/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.content.loader.MultiTypeProcessorBinder;
import com.almuradev.content.type.ContentType;
import com.almuradev.content.type.generation.processor.WeightProcessor;
import com.almuradev.content.type.generation.type.feature.grass.GrassGeneratorModule;
import com.almuradev.content.type.generation.type.feature.tree.TreeGeneratorModule;
import com.almuradev.content.type.generation.type.underground.ore.UndergroundOreGeneratorModule;
import com.almuradev.core.CoreBinder;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class GenerationModule extends AbstractModule implements CoreBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("generation", GenerationContentTypeLoader.class));
        this.facet().add(GenerationContentTypeLoader.class);
        this.install(new UndergroundOreGeneratorModule());
        this.install(new TreeGeneratorModule());
        this.install(new GrassGeneratorModule());
        this.install(new Module() {
            @Override
            protected void configure() {
                this.processors()
                        .all(WeightProcessor.class);
            }
        });
    }

    public static abstract class Module extends AbstractModule implements CoreBinder {
        protected final MultiTypeProcessorBinder<GenerationGenre, ContentGenerator, ContentGenerator.Builder<ContentGenerator>, GenerationContentProcessor<ContentGenerator, ContentGenerator.Builder<ContentGenerator>>> processors() {
            return new MultiTypeProcessorBinder<>(
                    this.binder(),
                    GenerationGenre.values(),
                    new TypeLiteral<GenerationGenre>() {},
                    new TypeLiteral<GenerationContentProcessor<ContentGenerator, ContentGenerator.Builder<ContentGenerator>>>() {}
            );
        }
    }
}
