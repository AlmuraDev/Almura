/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.config;

import com.almuradev.toolbox.config.map.AbstractMappedConfiguration;
import net.minecraftforge.common.MinecraftForge;

import java.nio.file.Path;

public abstract class AbstractPostingMappedConfiguration<T> extends AbstractMappedConfiguration<T> {

    protected AbstractPostingMappedConfiguration(final Class<T> type, final Path path) {
        super(type, path);
    }

    @Override
    public void load() {
        super.load();
        MinecraftForge.EVENT_BUS.post(new ConfigLoadEvent<>(this.type, this));
    }
}
