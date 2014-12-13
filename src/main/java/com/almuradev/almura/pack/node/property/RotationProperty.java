package com.almuradev.almura.pack.node.property;

import com.almuradev.almura.pack.IRotatable;

public class RotationProperty implements IProperty<Boolean> {
    private final boolean enabled;
    private final IRotatable.Rotation rotation;
    private final float angle;
    private final IRotatable.Direction x, y, z;

    public RotationProperty(boolean enabled, IRotatable.Rotation rotation, float angle, IRotatable.Direction x, IRotatable.Direction y,
                            IRotatable.Direction z) {
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

    public IRotatable.Rotation getRotation() {
        return rotation;
    }

    public float getAngle() {
        return angle;
    }

    public IRotatable.Direction getX() {
        return x;
    }

    public IRotatable.Direction getY() {
        return y;
    }

    public IRotatable.Direction getZ() {
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
