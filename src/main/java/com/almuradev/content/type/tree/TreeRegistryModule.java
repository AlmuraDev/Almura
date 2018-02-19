/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree;

import com.almuradev.content.registry.AbstractCatalogRegistryModule;
import com.almuradev.content.registry.EagerCatalogRegistration;

import javax.inject.Singleton;

@EagerCatalogRegistration
@Singleton
public final class TreeRegistryModule extends AbstractCatalogRegistryModule<Tree> {
    protected TreeRegistryModule() {
        super(1);
    }
}
