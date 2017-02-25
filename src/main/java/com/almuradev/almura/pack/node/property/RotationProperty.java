/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import com.almuradev.almura.pack.RotationMeta;

public class RotationProperty implements IProperty<Boolean> {

    private final boolean enabled;
    private final RotationMeta.Rotation rotation;
    private final float angle;
    private final RotationMeta.Direction x, y, z;

    public RotationProperty(boolean enabled, RotationMeta.Rotation rotation, float angle, RotationMeta.Direction x, RotationMeta.Direction y,
            RotationMeta.Direction z) {
        this.enabled = enabled;
        this.rotation = rotation;
        this.angle = angle;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Boolean getSource() {
        return enabled;
    }

    public RotationMeta.Rotation getRotation() {
        return rotation;
    }

    public float getAngle() {
        return angle;
    }

    public RotationMeta.Direction getX() {
        return x;
    }

    public RotationMeta.Direction getY() {
        return y;
    }

    public RotationMeta.Direction getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && rotation == ((RotationProperty) o).getRotation();

    }

    @Override
    public int hashCode() {
        return rotation.hashCode();
    }
}
