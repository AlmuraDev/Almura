/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.inject;

import com.almuradev.almura.shared.network.PacketBinder;
import com.almuradev.almura.shared.registry.binder.RegistryBinder;
import net.kyori.membrane.facet.Facet;
import net.kyori.membrane.facet.FacetBinder;
import net.kyori.violet.ForwardingBinder;

/**
 * Common binders.
 */
public interface CommonBinder extends ForwardingBinder {

    /**
     * Creates a facet binder.
     *
     * @return a facet binder
     * @see Facet
     */
    default FacetBinder facet() {
        return FacetBinder.create(this.binder());
    }

    /**
     * Creates a packet binder.
     *
     * @return a packet binder
     */
    default PacketBinder packet() {
        return PacketBinder.create(this.binder());
    }

    /**
     * Creates a registry binder.
     *
     * @return a registry binder
     */
    default RegistryBinder registry() {
        return RegistryBinder.create(this.binder());
    }
}
