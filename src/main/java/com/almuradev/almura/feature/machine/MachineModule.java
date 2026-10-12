/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorBlock;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class MachineModule extends AbstractModule implements CommonBinder {

    // Purpose: furnace-like machines, powered by IndustrialCraft 2 EU when it's installed.

    @Override
    protected void configure() {
        this.facet().add(MachineFeature.class);
        this.requestStaticInjection(SeedProcessorBlock.class);
    }
}
