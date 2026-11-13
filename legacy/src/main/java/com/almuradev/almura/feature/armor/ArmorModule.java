/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.armor;

import com.almuradev.almura.feature.armor.client.ClientDynamicLightManager;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class ArmorModule extends AbstractModule implements CommonBinder {

    // Purpose: hand coded armor, starting with the lighted helmet and its client side dynamic light.

    @Override
    protected void configure() {
        this.facet().add(ArmorFeature.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SideOnly(Side.CLIENT)
                protected void configure() {
                    this.facet().add(ClientDynamicLightManager.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
