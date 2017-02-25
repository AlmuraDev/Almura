/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

public class RenderNode implements INode<Boolean> {

    private final boolean normalCube;
    private final boolean opaque;
    private final boolean flipU;
    private final boolean flipV;

    public RenderNode(boolean normalCube, boolean opaque, boolean flipU, boolean flipV) {
        this.normalCube = normalCube;
        this.opaque = opaque;
        this.flipU = flipU;
        this.flipV = flipV;
    }

    @Override
    public Boolean getValue() {
        return normalCube;
    }

    public Boolean isOpaque() {
        return opaque;
    }

    public boolean isFlipU() {
        return flipU;
    }

    public boolean isFlipV() {
        return flipV;
    }
}
