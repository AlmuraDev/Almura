/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.perms;

import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class PermsModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet().add(PermsFeature.class);
    }
}
