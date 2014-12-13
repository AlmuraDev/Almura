package com.almuradev.almura.pack.node.property;

public class RotationProperty implements IProperty<Boolean> {
    private final boolean enabled;

    public RotationProperty(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Boolean getSource() {
        return enabled;
    }
}
