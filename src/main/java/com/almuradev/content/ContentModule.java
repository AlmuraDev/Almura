/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content;

import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.content.component.apply.ApplyParser;
import com.almuradev.content.component.apply.ApplyParserImpl;
import com.almuradev.content.loader.RootContentLoader;
import com.almuradev.content.loader.ClientTranslationInjector;
import com.almuradev.content.loader.ServerTranslationInjector;
import com.almuradev.content.loader.TranslationInjector;
import com.almuradev.content.type.action.ActionModule;
import com.almuradev.content.type.block.BlockModule;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroupModule;
import com.almuradev.content.type.item.ItemModule;
import com.almuradev.content.type.itemgroup.ItemGroupModule;
import com.almuradev.content.type.mapcolor.MapColorModule;
import com.almuradev.content.type.material.MaterialModule;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import org.spongepowered.api.Platform;

public final class ContentModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.bind(ApplyParser.class).to(ApplyParserImpl.class);
        this.facet()
                .add(RootContentLoader.class)
                .add(ServerTranslationInjector.class);
        this.install(new ActionModule());
        this.install(new BlockModule());
        this.install(new BlockSoundGroupModule());
        this.install(new ItemModule());
        this.install(new ItemGroupModule());
        this.install(new MapColorModule());
        this.install(new MaterialModule());
        this.inSet(TranslationInjector.class).addBinding().to(ServerTranslationInjector.class);
        this.on(Platform.Type.CLIENT, () -> {
            this.inSet(IResourceManagerReloadListener.class).addBinding().to(ClientTranslationInjector.class);
            this.inSet(TranslationInjector.class).addBinding().to(ClientTranslationInjector.class);
        });
    }
}
