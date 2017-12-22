/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.accessory.type.Accessory;
import com.google.common.base.MoreObjects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

import java.util.Objects;

public final class AlmuraAccessoryType implements AccessoryType {

    private final String id;
    private final String name;
    private final Class<? extends Accessory> accessoryClass;
    // TODO Someday, we may have multiple texture layers for an
    // accessory
    private final ResourceLocation[] layers;

    public AlmuraAccessoryType(String id, String name, Class<? extends Accessory> accessoryClass, ResourceLocation... layers) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AlmuraAccessoryType that = (AlmuraAccessoryType) o;
        return Objects.equals(id, that.id);
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
