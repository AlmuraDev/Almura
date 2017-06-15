/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.obj.material;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class MaterialDefinition {

    private final String name;
    private final ResourceLocation diffuseTexture;

    private MaterialDefinition(String name, ResourceLocation diffuseTexture) {
        this.name = name;
        this.diffuseTexture = diffuseTexture;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return this.name;
    }

    public Optional<ResourceLocation> getDiffuseTexture() {
        return Optional.ofNullable(this.diffuseTexture);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MaterialDefinition)) {
            return false;
        }

        final MaterialDefinition other = (MaterialDefinition) obj;
        return other.name.equals(this.name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("map_Kd", this.diffuseTexture)
                .toString();
    }

    public static final class Builder {

        private ResourceLocation diffuseTexture;

        public Builder diffuseTexture(ResourceLocation location) {
            this.diffuseTexture = location;
            return this;
        }

        public Builder from(MaterialDefinition materialDefinition) {
            this.diffuseTexture = materialDefinition.diffuseTexture;
            return this;
        }

        public MaterialDefinition build(String name) {
            checkState(name != null, "Name cannot be null!");
            checkState(!name.isEmpty(), "Name cannot be empty!");

            return new MaterialDefinition(name, this.diffuseTexture);
        }
    }
}
