/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.speed;

import com.almuradev.almura.shared.inject.ClientBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;

@SideOnly(Side.CLIENT)
public final class ClientOptimizationModule extends AbstractModule implements ClientBinder {

    @Override
    protected void configure() {
        this.requestStaticInjection(AlmuraSettings.class);
        Display.setTitle(Display.getTitle() + " - Almura 3.1 - build 103");
    }
}
