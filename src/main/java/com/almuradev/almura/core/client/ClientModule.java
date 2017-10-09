/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client;

import com.almuradev.almura.Constants;
import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.almura.core.common.CommonModule;
import com.almuradev.almura.feature.hud.ClientHeadUpDisplay;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIDetailsPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIWorldPanel;
import com.almuradev.almura.feature.menu.MainMenuFeature;
import com.almuradev.almura.feature.speed.FirstLaunchClientOptimization;
import com.almuradev.shared.client.model.ModelBinder;
import com.almuradev.shared.client.model.obj.OBJModelLoader;
import com.almuradev.shared.client.model.obj.OBJModelParser;
import com.almuradev.shared.inject.ClientBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.gui.GuiIngame;

/**
 * The root module for the client.
 */
public final class ClientModule extends AbstractModule implements ClientBinder {

    @Override
    protected void configure() {
        this.install(new CommonModule());
        this.install(new ClientConfigModule());
        this.facet()
                .add(ModelBinder.Installer.class)
                .add(MainMenuFeature.class)
                .add(ClientHeadUpDisplay.class)
                .add(FirstLaunchClientOptimization.class);
        this.model()
                .loader(OBJModelLoader.class, binder -> {
                    binder.domains(Constants.Plugin.ID);
                });
        this.requestStaticInjection(StaticAccess.class);
        this.requestStaticInjection(UIDetailsPanel.class);
        this.requestStaticInjection(UIWorldPanel.class);
        this.requestMixinInjection();
        this.installFactory(OBJModelParser.Factory.class);
    }

    // HACK: inject into required mixin target classes
    @SuppressWarnings("UnnecessaryStaticInjection")
    private void requestMixinInjection() {
        this.requestStaticInjection(GuiIngame.class);
    }
}
