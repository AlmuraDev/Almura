package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.IRotatable;
import com.almuradev.almura.pack.node.property.RotationProperty;

import java.util.EnumMap;

public class RotationNode extends ToggleableNode<EnumMap<IRotatable.Rotation, RotationProperty>> {
    private final boolean defaultRotate;
    private final boolean defaultMirrorRotate;
    private final EnumMap<IRotatable.Rotation, RotationProperty> value;

    public RotationNode(boolean isEnabled, boolean defaultRotate, boolean defaultMirrorRotate, EnumMap<IRotatable.Rotation, RotationProperty> value) {
        super(isEnabled);
        this.defaultRotate = defaultRotate;
        this.defaultMirrorRotate = defaultMirrorRotate;
        this.value = value;
    }

    @Override
    public EnumMap<IRotatable.Rotation, RotationProperty> getValue() {
        return value;
    }

    public boolean isDefaultRotate() {
        return defaultRotate;
    }

    public boolean isDefaultMirrorRotate() {
        return defaultMirrorRotate;
    }

    public RotationProperty getRotationProperty(IRotatable.Rotation rotation) {
        return value.get(rotation);
    }
}
