/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.accessory.AccessoryType;
import com.google.common.base.MoreObjects;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

public final class AlmuraAccessoryType implements AccessoryType {

    private final String id;
    private final String name;
    private final Class<? extends Accessory> accessoryClass;
    // TODO Someday, we may have multiple texture layers for an
    // accessory
    private final ResourceLocation[] layers;

    public AlmuraAccessoryType(final String id, final String name, final Class<? extends Accessory> accessoryClass, final ResourceLocation... layers) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(accessoryClass);
        checkNotNull(layers);

        this.id = id;
        this.name = name;
        this.accessoryClass = accessoryClass;
        this.layers = layers;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<? extends Accessory> getAccessoryClass() {
        return this.accessoryClass;
    }

    @Override
    public ResourceLocation[] getTextureLayers() {
        return this.layers;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AlmuraAccessoryType other = (AlmuraAccessoryType) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("name", this.name)
                .add("accessoryClass", this.accessoryClass)
                .add("layers", this.layers)
                .toString();
    }
}
