/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj.geometry;

import com.google.common.base.MoreObjects;

public final class VertexTextureCoordinate {
    private int index;
    private final float u, v, w;

    public VertexTextureCoordinate(final float u, final float v, final float w) {
        this.u = u;
        this.v = v;
        this.w = w;
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

    public float getU() {
        return this.u;
    }

    public float getV() {
        return this.v;
    }

    public float getW() {
        return this.w;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", this.index)
                .add("u", this.u)
                .add("v", this.v)
                .add("w", this.w)
                .toString();
    }
}
