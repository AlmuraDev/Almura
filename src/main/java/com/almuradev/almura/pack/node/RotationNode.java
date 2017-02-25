/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.RotationMeta;
import com.almuradev.almura.pack.node.property.RotationProperty;

import java.util.EnumMap;

public class RotationNode extends ToggleableNode<EnumMap<RotationMeta.Rotation, RotationProperty>> {

    private final boolean defaultRotate;
    private final boolean defaultMirrorRotate;
    private final EnumMap<RotationMeta.Rotation, RotationProperty> value;

    public RotationNode(boolean isEnabled, boolean defaultRotate, boolean defaultMirrorRotate,
            EnumMap<RotationMeta.Rotation, RotationProperty> value) {
        super(isEnabled);
        this.defaultRotate = defaultRotate;
        this.defaultMirrorRotate = defaultMirrorRotate;
        this.value = value;
    }

    @Override
    public EnumMap<RotationMeta.Rotation, RotationProperty> getValue() {
        return value;
    }

    public boolean isDefaultRotate() {
        return defaultRotate;
    }

    public boolean isDefaultMirrorRotate() {
        return defaultMirrorRotate;
    }

    public RotationProperty getRotationProperty(RotationMeta.Rotation rotation) {
        return value.get(rotation);
    }
}
