/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.item;

import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.LoaderPhase;
import com.almuradev.almura.content.type.item.builder.AbstractItemTypeBuilder;
import com.almuradev.almura.content.type.item.factory.ItemItemGroupProvider;
import com.almuradev.almura.content.type.item.factory.SetItemAttributesTask;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.group.ItemGroupBuilderImpl;
import com.almuradev.almura.content.type.item.group.factory.SetItemGroupAttributesTask;
import com.almuradev.almura.content.type.item.registry.ItemGroupRegistryModule;
import com.almuradev.almura.content.type.item.type.BuildableItemType;
import com.almuradev.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public class ItemModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.registry()
                .builder(ItemGroup.Builder.class, ItemGroupBuilderImpl::new)
                .module(ItemGroup.class, ItemGroupRegistryModule.getInstance())
                .builder(BuildableItemType.Builder.class, AbstractItemTypeBuilder.BuilderImpl::new);
        this.asset()
                .provider(SetItemGroupAttributesTask.class, binder -> {
                    binder.phase(LoaderPhase.CONSTRUCTION);
                    binder.type(Asset.Type.ITEMGROUP);
                })
                .provider(ItemItemGroupProvider.class, binder -> {
                    binder.phase(LoaderPhase.CONSTRUCTION);
                    binder.type(Asset.Type.ITEM);
                })
                .provider(SetItemAttributesTask.class, binder -> {
                    binder.phase(LoaderPhase.CONSTRUCTION);
                    binder.type(Asset.Type.ITEM);
                });
    }
}
