/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.model.obj.geometry;

import com.flowpowered.math.vector.Vector3f;
import com.google.common.base.MoreObjects;

public class VertexNormal extends Vector3f {

    private int index;

    public VertexNormal(float x, float y, float z) {
        super(x, y, z);
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("index", this.index)
                .add("x", this.getX())
                .add("y", this.getY())
                .add("z", this.getZ())
                .toString();
    }
}
