/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.client.model.obj.geometry;

import static com.google.common.base.Preconditions.checkState;

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

    private VertexDefinition(Vertex vertex, VertexTextureCoordinate textureCoordinate, VertexNormal normal) {
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

    public void setIndex(int index) {
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
    public boolean equals(Object obj) {
        if (!(obj instanceof VertexDefinition)) {
            return false;
        }

        final VertexDefinition other = (VertexDefinition) obj;
        return Objects.equal(other.vertex, this.vertex) && Objects.equal(other.textureCoordinate, this.textureCoordinate) && Objects.equal(other
                .normal, this.normal);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
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

        public Builder vertex(Vertex vertex) {
            this.vertex = vertex;
            return this;
        }

        public Builder textureCoordinate(VertexTextureCoordinate textureCoordinate) {
            this.textureCoordinate = textureCoordinate;
            return this;
        }

        public Builder normal(VertexNormal normal) {
            this.normal = normal;
            return this;
        }

        public VertexDefinition build() {
            checkState(this.vertex != null, "Vertex cannot be null!");

            return new VertexDefinition(this.vertex, this.textureCoordinate, this.normal);
        }
    }
}
