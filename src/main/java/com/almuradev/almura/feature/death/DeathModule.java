/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death;

import com.almuradev.almura.feature.death.client.gui.PlayerDiedGUI;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class DeathModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.on(Platform.Type.SERVER, () -> {
            final class ServerModule extends AbstractModule implements CommonBinder {

                @SideOnly(Side.SERVER)
                @Override
                protected void configure() {
                    this.facet().add(DeathHandler.class);
                    this.requestStaticInjection(DeathModule.class);
                }
            }
            this.install(new ServerModule());
        });

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
                protected void configure() {
                    this.requestStaticInjection(PlayerDiedGUI.class);

                }
            }
            this.install(new ClientModule());
        });
    }
}
