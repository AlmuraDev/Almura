/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component;

import com.almuradev.content.component.apply.ApplyModule;
import net.kyori.violet.AbstractModule;

public final class ComponentModule extends AbstractModule {
    @Override
    protected void configure() {
        this.install(new ApplyModule());
    }

    public static abstract class Module extends AbstractModule {
    }
}
