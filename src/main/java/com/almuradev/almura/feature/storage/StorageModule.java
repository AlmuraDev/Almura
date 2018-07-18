/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage;

import com.almuradev.almura.feature.storage.block.StorageBlock;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class StorageModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet().add(StorageFeature.class);
        this.requestStaticInjection(StorageBlock.class);
    }
}
