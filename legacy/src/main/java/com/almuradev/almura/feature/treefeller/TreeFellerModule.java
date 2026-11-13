/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.treefeller;

import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class TreeFellerModule extends AbstractModule implements CommonBinder {

    // Purpose: cutting the log of a fruit tree fells the whole tree, dropping its logs, leaves and fruit where they were.

    @Override
    protected void configure() {
        this.facet().add(ServerTreeFellerManager.class);
    }
}
