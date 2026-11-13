/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage;

import com.almuradev.almura.feature.storage.client.StorageBarrelModels;
import com.almuradev.almura.feature.storage.block.StorageBlock;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class StorageModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet().add(StorageFeature.class);
        this.requestStaticInjection(StorageBlock.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {
                    // The storage barrel's label
                    this.facet().add(StorageBarrelModels.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
