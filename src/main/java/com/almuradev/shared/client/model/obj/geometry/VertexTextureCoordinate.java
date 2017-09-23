/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.client.model.obj.geometry;

import com.flowpowered.math.vector.Vector3f;
import com.google.common.base.MoreObjects;

public class VertexTextureCoordinate extends Vector3f {

    private int index;

    public VertexTextureCoordinate(float u, float v, float w) {
        super(u, v, w);
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

    public float getU() {
        return this.getX();
    }

    public float getV() {
        return this.getY();
    }

    public float getW() {
        return this.getZ();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", this.index)
                .add("u", this.getX())
                .add("v", this.getY())
                .add("w", this.getZ())
                .toString();
    }
}
