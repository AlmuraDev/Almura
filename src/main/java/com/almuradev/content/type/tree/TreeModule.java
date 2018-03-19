/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree;

import com.almuradev.content.loader.SingleTypeProcessorBinder;
import com.almuradev.content.type.ContentType;
import com.almuradev.content.type.tree.processor.BigProcessor;
import com.almuradev.content.type.tree.processor.FruitProcessor;
import com.almuradev.content.type.tree.processor.HangingProcessor;
import com.almuradev.content.type.tree.processor.HeightProcessor;
import com.almuradev.content.type.tree.processor.LeavesProcessor;
import com.almuradev.content.type.tree.processor.LogProcessor;
import com.almuradev.core.CoreBinder;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class TreeModule extends AbstractModule implements CoreBinder {
    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("tree", TreeContentTypeLoader.class));
        this.facet().add(TreeContentTypeLoader.class);
        this.bind(Tree.Builder.class).to(TreeBuilder.class);
        this.registry().module(Tree.class, TreeRegistryModule.class);
        this.processors()
                .add(BigProcessor.class)
                .add(FruitProcessor.class)
                .add(HangingProcessor.class)
                .add(HeightProcessor.class)
                .add(LeavesProcessor.class)
                .add(LogProcessor.class);
    }

    private SingleTypeProcessorBinder<Tree, Tree.Builder, ConfigProcessor<? extends Tree.Builder>> processors() {
        return new SingleTypeProcessorBinder<>(this.binder(), new TypeLiteral<ConfigProcessor<? extends Tree.Builder>>() {});
    }
}
