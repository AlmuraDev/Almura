/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.obj.geometry;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.client.model.obj.material.MaterialDefinition;
import com.google.common.base.Objects;

import java.util.LinkedHashSet;
import java.util.Optional;

public class Group {

    private final String name;
    private final MaterialDefinition materialDefinition;
    private final LinkedHashSet<Face> faces;

    private Group(String name, MaterialDefinition materialDefinition, LinkedHashSet<Face> faces) {
        this.name = name;
        this.materialDefinition = materialDefinition;
        this.faces = faces;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return this.name;
    }

    public Optional<MaterialDefinition> getMaterialDefinition() {
        return Optional.ofNullable(this.materialDefinition);
    }

    public LinkedHashSet<Face> getFaces() {
        return this.faces;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Group)) {
            return false;
        }

        final Group other = (Group) obj;
        return other.name.equals(this.name);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", this.name)
                .add("materialDefinition", this.materialDefinition)
                .add("faces", this.faces)
                .toString();
    }

    public static final class Builder {

        private MaterialDefinition materialDefinition;
        private LinkedHashSet<Face> faces = new LinkedHashSet<>();

        public Builder materialDefinition(MaterialDefinition materialDefinition) {
            this.materialDefinition = materialDefinition;
            return this;
        }

        public Builder face(Face face) {
            this.faces.add(face);
            face.setIndex(this.faces.size());
            return this;
        }

        public Group build(String name) {
            checkState(name != null, "Name cannot be null!");
            checkState(!name.isEmpty(), "Name cannot be empty!");
            checkState(!this.faces.isEmpty(), "Faces cannot be empty!");

            return new Group(name, this.materialDefinition, this.faces);
        }
    }
}
