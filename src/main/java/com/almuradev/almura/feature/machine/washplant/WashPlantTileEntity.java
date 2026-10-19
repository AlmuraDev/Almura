/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.washplant;

import com.almuradev.almura.feature.machine.processing.MachineType;
import com.almuradev.almura.feature.machine.processing.ProcessingTileEntity;

public final class WashPlantTileEntity extends ProcessingTileEntity {

    @Override
    public MachineType type() {
        return MachineType.WASHPLANT;
    }
}
