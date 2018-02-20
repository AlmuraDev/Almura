/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj.geometry;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;

import java.util.LinkedHashSet;
import java.util.LinkedList;

public final class Face {

    private final LinkedList<VertexDefinition> vertices;
    private int index;

    private Face(final LinkedList<VertexDefinition> vertices) {
        this.vertices = vertices;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(final int index) {
        if (this.index > 0) {
            throw new IllegalStateException("Cannot re-set index!");
        }

        this.index = index;
    }

    public LinkedList<VertexDefinition> getVertices() {
        return this.vertices;
    }

    @Override
    public boolean equals(final Object obj) {
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

        private LinkedList<VertexDefinition> vertices = new LinkedList<>();

        public Builder vertex(final VertexDefinition vertex) {
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
