/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.asm.interfaces;

import net.minecraftforge.common.capabilities.CapabilityDispatcher;

public interface IMixinItemStack {

    CapabilityDispatcher getCapabilities();
}
