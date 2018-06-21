/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death;

import com.almuradev.almura.feature.speed.FirstLaunchOptimization;
import com.almuradev.almura.shared.inject.ClientBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public final class DeathModule extends AbstractModule implements ClientBinder {

    @Override
    protected void configure() {
        this.facet().add(DeathHandler.class);
        this.requestStaticInjection(DeathModule.class);
    }
}
