/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.capability;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * A standard {@link IItemHandler} whose contract now enforces direct slot manipulation.
 */
public interface IMultiSlotItemHandler extends IItemHandlerModifiable {

    /**
     * Sets the amount of slots this handler will operate on. Great care must be taken in changing the slot count as
     * setting the value lower than what {@link IItemHandler#getSlots()} will result in loss of data when the handler is
     * saved next (if ever).
     *
     * @param slotCount The new slot count
     */
    void resize(int slotCount);
}
