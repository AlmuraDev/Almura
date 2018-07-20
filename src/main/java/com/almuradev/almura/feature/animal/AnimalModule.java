/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal;

import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class AnimalModule extends AbstractModule implements CommonBinder {

    //Purpose:  This feature sets an animals name a specific colors for certain breeding events.

    @Override
    protected void configure() {
        this.facet().add(ServerAnimalManager.class);
    }
}