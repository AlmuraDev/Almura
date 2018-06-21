/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.permission;

import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class PermissionsModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet().add(PermissionsManager.class);
    }
}
