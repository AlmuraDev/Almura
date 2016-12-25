package com.almuradev.almura.client.gui.component.entity;

public class RenderEntityAngle {

    public final float pitch, yaw, roll;

    protected RenderEntityAngle(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static class Builder {
        private float pitch = 0f, yaw = 0f, roll = 0f;

        public Builder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder yaw(float yaw) {
            this.yaw = yaw;
            return this;
        }

        public Builder roll(float roll) {
            this.roll = roll;
            return this;
        }

        public RenderEntityAngle build() {
            return new RenderEntityAngle(this.pitch, this.yaw, this.roll);
        }
    }

    public static class Angles {
        public static final RenderEntityAngle FRONT = RenderEntityAngle.builder().yaw(0).build();
        public static final RenderEntityAngle SIDE_RIGHT = RenderEntityAngle.builder().yaw(90).build();
        public static final RenderEntityAngle BACK = RenderEntityAngle.builder().yaw(180).build();
        public static final RenderEntityAngle SIDE_LEFT = RenderEntityAngle.builder().yaw(270).build();
    }
}