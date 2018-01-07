/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.ContentType;
import com.almuradev.content.loader.MultiTypeProcessorBinder;
import com.almuradev.content.type.generation.type.ore.OreGeneratorModule;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class GenerationModule extends AbstractModule implements CommonBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("generation", GenerationContentTypeLoader.class));
        this.facet().add(GenerationContentTypeLoader.class);
        this.install(new OreGeneratorModule());
    }

    public static abstract class Module extends AbstractModule implements CommonBinder {
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
