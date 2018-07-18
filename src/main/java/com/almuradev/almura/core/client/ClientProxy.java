/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.shared.plugin.Plugin;
import com.google.inject.Injector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The client bootstrap.
 */
@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy {
    @Override
    protected Injector createInjector(final Plugin plugin, final Injector parent) {
        return parent.createChildInjector(new ClientModule(plugin));
    }
}
