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
import com.google.common.base.Objects;

import java.util.Optional;

/**
 * Represents the combined data at a {@link Vertex} contained in a {@link Face}.
 *
 * This data comprises of {@link Vertex}, an optional {@link VertexTextureCoordinate} and an
 * optional {@link VertexNormal}.
 */
public class VertexDefinition {
    private final Vertex vertex;
    private final VertexTextureCoordinate textureCoordinate;
    private final VertexNormal normal;
    private int index;

    private VertexDefinition(final Vertex vertex, final VertexTextureCoordinate textureCoordinate, final VertexNormal normal) {
        this.vertex = vertex;
        this.textureCoordinate = textureCoordinate;
        this.normal = normal;
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

    public Vertex getVertex() {
        return this.vertex;
    }

    public Optional<VertexTextureCoordinate> getTextureCoordinate() {
        return Optional.ofNullable(this.textureCoordinate);
    }

    public Optional<VertexNormal> getNormal() {
        return Optional.ofNullable(this.normal);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof VertexDefinition)) {
            return false;
        }

        final VertexDefinition other = (VertexDefinition) obj;
        return Objects.equal(other.vertex, this.vertex) && Objects.equal(other.textureCoordinate, this.textureCoordinate) && Objects.equal(other
                .normal, this.normal);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", this.index)
                .add("vertex", this.vertex)
                .add("textureCoordinate", this.textureCoordinate)
                .add("normal", this.normal)
                .toString();
    }

    public static class Builder {
        private Vertex vertex;
        private VertexTextureCoordinate textureCoordinate;
        private VertexNormal normal;

        public Builder vertex(final Vertex vertex) {
            this.vertex = vertex;
            return this;
        }

        public Builder textureCoordinate(final VertexTextureCoordinate textureCoordinate) {
            this.textureCoordinate = textureCoordinate;
            return this;
        }

        public Builder normal(final VertexNormal normal) {
            this.normal = normal;
            return this;
        }

        public VertexDefinition build() {
            checkState(this.vertex != null, "Vertex cannot be null!");

            return new VertexDefinition(this.vertex, this.textureCoordinate, this.normal);
        }
    }
}
