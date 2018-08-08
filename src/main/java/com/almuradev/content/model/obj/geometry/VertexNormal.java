/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj.geometry;

import com.google.common.base.MoreObjects;

public final class VertexNormal {
    private int index;
    private final float x, y, z;

    public VertexNormal(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", this.index)
                .add("x", this.x)
                .add("y", this.y)
                .add("z", this.z)
                .toString();
    }
}
