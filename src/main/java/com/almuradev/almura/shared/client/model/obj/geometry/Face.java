/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.model.obj.geometry;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;

import java.util.LinkedHashSet;

public class Face {

    private final LinkedHashSet<VertexDefinition> vertices;
    private int index;

    private Face(LinkedHashSet<VertexDefinition> vertices) {
        this.vertices = vertices;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        if (this.index > 0) {
            throw new IllegalStateException("Cannot re-set index!");
        }

        this.index = index;
    }

    public LinkedHashSet<VertexDefinition> getVertices() {
        return this.vertices;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Face)) {
            return false;
        }

        final Face other = (Face) obj;
        return other.index == this.index && other.vertices.equals(this.vertices);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", this.index)
                .add("vertices", this.vertices)
                .toString();
    }

    public static final class Builder {

        private LinkedHashSet<VertexDefinition> vertices = new LinkedHashSet<>();

        public Builder vertex(VertexDefinition vertex) {
            this.vertices.add(vertex);
            vertex.setIndex(this.vertices.size());
            return this;
        }

        public Face build() {
            checkState(!this.vertices.isEmpty(), "Vertex definitions cannot be empty!");

            return new Face(this.vertices);
        }
    }
}
