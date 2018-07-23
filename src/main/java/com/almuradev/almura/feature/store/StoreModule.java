/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store;

import com.almuradev.almura.feature.store.client.gui.StoreItemsModifyScreen;
import com.almuradev.almura.feature.store.client.gui.StoreScreen;
import com.almuradev.almura.feature.store.client.gui.StoreListScreen;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class StoreModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.requestStaticInjection(StoreScreen.class);
                    this.requestStaticInjection(StoreListScreen.class);
                    this.requestStaticInjection(StoreItemsModifyScreen.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
