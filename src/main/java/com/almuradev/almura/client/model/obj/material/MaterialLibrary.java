/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.obj.material;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Objects;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class MaterialLibrary {

    private final ResourceLocation source;
    private final String name;
    private final Set<MaterialDefinition> materialDefinitions;

    private MaterialLibrary(ResourceLocation source, String name, Set<MaterialDefinition> materialDefinitions) {
        this.source = source;
        this.name = name;
        this.materialDefinitions = materialDefinitions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ResourceLocation getSource() {
        return this.source;
    }

    public String getName() {
        return this.name;
    }

    public Set<MaterialDefinition> getMaterialDefinitions() {
        return this.materialDefinitions;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MaterialLibrary)) {
            return false;
        }

        final MaterialLibrary other = (MaterialLibrary) obj;
        return other.name.equals(this.name);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", this.name)
                .add("source", this.source)
                .add("materialDefinitions", this.materialDefinitions)
                .toString();
    }

    public static final class Builder {

        private Set<MaterialDefinition> materialDefinitions = new HashSet<>();

        public Builder materialDefinition(MaterialDefinition materialDefinition) {
            this.materialDefinitions.add(materialDefinition);
            return this;
        }

        public Set<MaterialDefinition> materialDefinitions() {
            return this.materialDefinitions;
        }

        public Builder from(MaterialLibrary materialLibrary) {
            checkNotNull(materialLibrary);

            for (MaterialDefinition definition : materialLibrary.getMaterialDefinitions()) {
                this.materialDefinitions.add(MaterialDefinition.builder()
                        .from(definition)
                        .build(definition.getName())
                );
            }

            return this;
        }

        public MaterialLibrary build(ResourceLocation source, String name) {
            checkState(source != null, "Source cannot be null!");
            checkState(name != null, "Name cannot be null!");
            checkState(!name.isEmpty(), "Name cannot be empty!");
            checkState(!this.materialDefinitions.isEmpty(), "A material library must have at least one material definition!");

            return new MaterialLibrary(source, name, materialDefinitions);
        }
    }
}
