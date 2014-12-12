package com.almuradev.almura.pack.property;

public class RenderProperty implements IProperty<Boolean> {
    private final boolean normalCube, opaque;

    public RenderProperty(boolean normalCube, boolean opaque) {
        this.normalCube = normalCube;
        this.opaque = opaque;
    }

    @Override
    public Boolean getValue() {
        return normalCube;
    }

    public Boolean isOpaque() {
        return opaque;
    }
}
