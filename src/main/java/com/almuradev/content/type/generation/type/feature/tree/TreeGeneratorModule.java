/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.tree;

import com.almuradev.content.type.generation.GenerationGenre;
import com.almuradev.content.type.generation.GenerationModule;
import com.almuradev.content.type.generation.type.feature.tree.processor.BigTreeProcessor;
import com.almuradev.content.type.generation.type.feature.tree.processor.ChanceProcessor;
import com.almuradev.content.type.generation.type.feature.tree.processor.RequiresProcessor;
import com.almuradev.content.type.generation.type.feature.tree.processor.TreeProcessor;
import com.almuradev.content.type.generation.type.feature.tree.processor.WorldsProcessor;

public final class TreeGeneratorModule extends GenerationModule.Module {
    @Override
    protected void configure() {
        this.bind(TreeGenerator.Builder.class).to(TreeGeneratorBuilder.class);
        this.processors()
                .only(BigTreeProcessor.class, GenerationGenre.TREE_FEATURE)
                .only(ChanceProcessor.class, GenerationGenre.TREE_FEATURE)
                .only(RequiresProcessor.class, GenerationGenre.TREE_FEATURE)
                .only(TreeProcessor.class, GenerationGenre.TREE_FEATURE)
                .only(WorldsProcessor.class, GenerationGenre.TREE_FEATURE);
    }
}
