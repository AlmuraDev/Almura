/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.UISkillsPanel;
import com.almuradev.almura.feature.skills.client.ClientSkillsManager;
import com.almuradev.almura.feature.skills.client.gui.SkillsScreen;
import com.almuradev.almura.feature.skills.network.ClientboundSkillsPacket;
import com.almuradev.almura.feature.skills.network.handler.ClientboundSkillsPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class SkillsModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {

        this.packet().bind(ClientboundSkillsPacket.class, binder -> binder.handler(ClientboundSkillsPacketHandler.class, Platform.Type.CLIENT));

        this.facet().add(SkillsHandler.class);
        this.facet().add(ServerSkillsManager.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
                protected void configure() {
                    this.facet().add(ClientSkillsManager.class);
                    this.requestStaticInjection(SkillsScreen.class);
                    this.requestStaticInjection(UISkillsPanel.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
