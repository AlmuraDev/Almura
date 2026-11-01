/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.relight;

import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class RelightModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.requestStaticInjection(RelightCommand.class);
        this.command().child(RelightCommand.generateRelightCommand(), "relight");
    }
}
