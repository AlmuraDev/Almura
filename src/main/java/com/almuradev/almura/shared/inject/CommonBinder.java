/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.inject;

import com.almuradev.almura.shared.capability.binder.CapabilityBinder;
import com.almuradev.almura.shared.command.binder.CommandBinder;
import com.almuradev.almura.shared.network.PacketBinder;
import com.almuradev.core.CoreBinder;

/**
 * Common binders.
 */
public interface CommonBinder extends CoreBinder {
    /**
     * Creates a packet binder.
     *
     * @return a packet binder
     */
    default PacketBinder packet() {
        return PacketBinder.create(this.binder());
    }

    /**
     * Creates a command binder.
     *
     * @return a command binder
     */
    default CommandBinder command() {
        return CommandBinder.create(this.binder());
    }

    /**
     * Creates a capability binder.
     *
     * @return a capability binder
     */
    default CapabilityBinder capability() {
        return CapabilityBinder.create(this.binder());
    }
}
