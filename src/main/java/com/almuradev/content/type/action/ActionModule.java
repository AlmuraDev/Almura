/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action;

import com.almuradev.content.ContentType;
import com.almuradev.content.loader.MultiTypeExternalContentProcessor;
import com.almuradev.content.loader.MultiTypeProcessorBinder;
import com.almuradev.content.type.action.component.drop.DropParser;
import com.almuradev.content.type.action.component.drop.DropParserImpl;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyActionModule;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;

public final class ActionModule extends AbstractModule {

    @Override
    protected void configure() {
        this.inSet(ContentType.class).addBinding().toInstance(new ContentType.Impl("action", ActionContentTypeLoader.class));
        this.bind(new TypeLiteral<MultiTypeExternalContentProcessor<ActionGenre, ActionContentType, ActionContentType.Builder<ActionContentType>>>() {}).to(ActionContentTypeLoader.class);
        this.bind(DropParser.class).to(DropParserImpl.class);
        this.install(new BlockDestroyActionModule());
    }

    public static abstract class Module extends AbstractModule {
        protected final MultiTypeProcessorBinder<ActionGenre, ActionContentType, ActionContentType.Builder<ActionContentType>, ActionContentProcessor<ActionContentType, ActionContentType.Builder<ActionContentType>>> processors() {
            return new MultiTypeProcessorBinder<>(
                    this.binder(),
                    ActionGenre.values(),
                    new TypeLiteral<ActionGenre>() {},
                    new TypeLiteral<ActionContentProcessor<ActionContentType, ActionContentType.Builder<ActionContentType>>>() {}
            );
        }
    }
}
