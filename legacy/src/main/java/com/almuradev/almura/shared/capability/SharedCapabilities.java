/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public final class SharedCapabilities {

    @CapabilityInject(ISingleSlotItemHandler.class)
    public static Capability<ISingleSlotItemHandler> SINGLE_SLOT_ITEM_HANDLER_CAPABILITY = null;

    @CapabilityInject(IMultiSlotItemHandler.class)
    public static Capability<IMultiSlotItemHandler> MULTI_SLOT_ITEM_HANDLER_CAPABILITY = null;
}
