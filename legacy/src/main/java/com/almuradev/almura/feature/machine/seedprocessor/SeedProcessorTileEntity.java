/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.seedprocessor;

import com.almuradev.almura.feature.machine.processing.MachineType;
import com.almuradev.almura.feature.machine.processing.ProcessingTileEntity;

/**
 * The seed processor's tile entity (registered as almura:machine/seed_processor; same class and saved data as before the machines shared
 * {@link ProcessingTileEntity}, so placed seed processors keep working).
 */
public final class SeedProcessorTileEntity extends ProcessingTileEntity {

    @Override
    public MachineType type() {
        return MachineType.SEED_PROCESSOR;
    }
}
