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
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIDetailsPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIUserPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIWorldPanel;
import com.almuradev.almura.feature.menu.MainMenuModule;
import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.speed.ClientOptimizationModule;
import com.almuradev.almura.feature.title.ClientTitleManager;
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
        this.install(new CommonModule());
        this.install(new MainMenuModule());
        this.install(new ClientOptimizationModule());
        this.facet().add(ModelBinder.Installer.class);
        this.install(new ClientConfiguration.Module());
        this.model().loader(OBJModelLoader.class, binder -> binder.domains(Almura.ID));
        this.requestStaticInjection(StaticAccess.class);
        this.installFactory(OBJModelParser.Factory.class);
    }
}
