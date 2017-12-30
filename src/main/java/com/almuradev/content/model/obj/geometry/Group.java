/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj.geometry;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.content.model.obj.material.MaterialDefinition;
import com.google.common.base.MoreObjects;

import java.util.LinkedHashSet;
import java.util.Optional;

public class Group {

    private final String name;
    private final MaterialDefinition materialDefinition;
    private final LinkedHashSet<Face> faces;

    private Group(final String name, final MaterialDefinition materialDefinition, final LinkedHashSet<Face> faces) {
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
    public boolean equals(final Object obj) {
        if (!(obj instanceof Group)) {
            return false;
        }

        final Group other = (Group) obj;
        return other.name.equals(this.name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("materialDefinition", this.materialDefinition)
                .add("faces", this.faces)
                .toString();
    }

    public static final class Builder {

        private MaterialDefinition materialDefinition;
        private LinkedHashSet<Face> faces = new LinkedHashSet<>();

        public Builder materialDefinition(final MaterialDefinition materialDefinition) {
            this.materialDefinition = materialDefinition;
            return this;
        }

        public Builder face(final Face face) {
            this.faces.add(face);
            face.setIndex(this.faces.size());
            return this;
        }

        public Group build(final String name) {
            checkState(name != null, "Name cannot be null!");
            checkState(!name.isEmpty(), "Name cannot be empty!");
            checkState(!this.faces.isEmpty(), "Faces cannot be empty!");

            return new Group(name, this.materialDefinition, this.faces);
        }
    }
}
