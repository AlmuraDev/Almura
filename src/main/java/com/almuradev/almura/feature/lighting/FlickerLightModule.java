/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.lighting;

import com.almuradev.almura.feature.lighting.client.ClientFlickerManager;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class FlickerLightModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet().add(FlickerLightFeature.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {
                    this.facet().add(ClientFlickerManager.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
