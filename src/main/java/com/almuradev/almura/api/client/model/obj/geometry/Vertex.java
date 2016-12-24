/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.client.model.obj.geometry;

import com.flowpowered.math.vector.Vector4f;
import com.google.common.base.Objects;

public class Vertex extends Vector4f {

    private int index;

    public Vertex(float x, float y, float z, float w) {
        super(x, y, z, w);
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
        return Objects.toStringHelper(this)
                .add("index", this.index)
                .add("x", this.getX())
                .add("y", this.getY())
                .add("z", this.getZ())
                .toString();
    }
}
