/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj.geometry;

import com.google.common.base.MoreObjects;

public final class Vertex {
    private int index;
    private final float x, y, z, w;

    public Vertex(final float x, final float y, final float z, final float w) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public float getW() {
        return this.w;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", this.index)
                .add("x", this.x)
                .add("y", this.y)
                .add("z", this.z)
                .add("w", this.w)
                .toString();
    }
}
