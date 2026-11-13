/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.kitchen;

import com.almuradev.almura.feature.machine.processing.MachineType;
import com.almuradev.almura.feature.machine.processing.ProcessingTileEntity;

/**
 * The kitchen grinder: makes what crafting with the grinder would.
 */
public final class KitchenGrinderTileEntity extends ProcessingTileEntity {

    @Override
    public MachineType type() {
        return MachineType.KITCHEN_GRINDER;
    }
}
