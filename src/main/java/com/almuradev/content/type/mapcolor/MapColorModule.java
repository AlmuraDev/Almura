/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.mapcolor;

import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class MapColorModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.registry().module(MapColor.class, MapColorRegistryModule.class);
    }
}
