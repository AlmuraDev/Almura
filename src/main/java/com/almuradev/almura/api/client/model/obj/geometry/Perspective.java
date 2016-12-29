/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.client.model.obj.geometry;

import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3f;
import com.google.common.base.Objects;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.vecmath.Matrix4f;

public class Perspective {

    private final ItemCameraTransforms.TransformType transformType;
    private final TRSRTransformation transform;
    private Perspective(ItemCameraTransforms.TransformType transformType, TRSRTransformation transform) {
        this.transformType = transformType;
        this.transform = transform;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ItemCameraTransforms.TransformType getTransformType() {
        return this.transformType;
    }

    public TRSRTransformation getTransform() {
        return this.transform;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Perspective)) {
            return false;
        }

        final Perspective other = (Perspective) obj;
        return other.transformType.equals(this.transformType);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("transformType", this.transformType)
                .add("transformation", this.transform)
                .toString();
    }

    public static final class Builder {

        private ItemCameraTransforms.TransformType transformType = ItemCameraTransforms.TransformType.GUI;
        private Vector3f translation = Vector3f.ZERO;
        private Vector3f rotation = Vector3f.ZERO;
        private Vector3f scale = Vector3f.ZERO;

        public Builder transformType(ItemCameraTransforms.TransformType transformType) {
            this.transformType = transformType;
            return this;
        }

        public Builder translation(Vector3f translation) {
            this.translation = translation;
            return this;
        }

        public Builder rotation(Vector3f rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder scale(Vector3f scale) {
            this.scale = scale;
            return this;
        }

        public Perspective build() {
            checkState(transformType != null, "Transform type cannot be null!");
            checkState(this.translation != null, "Translation cannot be null!");
            checkState(this.rotation != null, "Rotation cannot be null!");
            checkState(this.scale != null, "Scale cannot be null!");

            final Matrix4f transformation = new Matrix4f();
            transformation.setIdentity();

            if (!this.rotation.equals(Vector3f.ZERO)) {
                final Matrix4f opMatrix = new Matrix4f();
                // TODO Check gl book to see why I need to have an op matrix here...
                opMatrix.rotX((float) Math.toRadians(this.rotation.getX()));
                transformation.mul(opMatrix);
                opMatrix.rotY((float) Math.toRadians(this.rotation.getY()));
                transformation.mul(opMatrix);
                opMatrix.rotZ((float) Math.toRadians(this.rotation.getZ()));
                transformation.mul(opMatrix);
            }

            if (!this.scale.equals(Vector3f.ZERO) && this.scale.getX() != 0f) {
                transformation.setScale(this.scale.getX());
            }

            if (!translation.equals(Vector3f.ZERO)) {
                transformation.setTranslation(new javax.vecmath.Vector3f(this.translation.getX(), this.translation.getY(), this.translation.getZ()));
            }

            return new Perspective(transformType, new TRSRTransformation(transformation));
        }
    }
}
