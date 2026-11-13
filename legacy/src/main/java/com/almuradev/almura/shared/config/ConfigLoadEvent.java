/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.config;

import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

public final class ConfigLoadEvent<C> extends GenericEvent<C> {

    private final MappedConfiguration<C> configAdapter;

    public ConfigLoadEvent(final Class<C> type, final MappedConfiguration<C> configAdapter) {
        super(type);
        this.configAdapter = configAdapter;
    }

    public MappedConfiguration<C> adapter() {
        return this.configAdapter;
    }

    public C configAdapter() {
        return this.configAdapter.get();
    }
}
