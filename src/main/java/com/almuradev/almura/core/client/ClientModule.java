/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.almura.core.common.CommonModule;
import com.almuradev.almura.feature.accessory.AccessoryFeature;
import com.almuradev.almura.feature.hud.ClientHeadUpDisplay;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIDetailsPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIWorldPanel;
import com.almuradev.almura.feature.menu.MainMenuFeature;
import com.almuradev.almura.feature.speed.FirstLaunchClientOptimization;
import com.almuradev.almura.shared.client.model.ModelBinder;
import com.almuradev.almura.shared.client.model.obj.OBJModelLoader;
import com.almuradev.almura.shared.client.model.obj.OBJModelParser;
import com.almuradev.almura.shared.inject.ClientBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The root module for the client.
 */
@SideOnly(Side.CLIENT)
public final class ClientModule extends AbstractModule implements ClientBinder {

    @Override
    protected void configure() {
        this.facet()
                .add(ModelBinder.Installer.class)
                .add(MainMenuFeature.class)
                .add(ClientHeadUpDisplay.class)
                .add(FirstLaunchClientOptimization.class)
                .add(AccessoryFeature.class);
        this.install(new ClientConfiguration.Module());
        this.model()
                .loader(OBJModelLoader.class, binder -> {
                    binder.domains(Almura.ID);
                });
        this.install(new CommonModule());
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
