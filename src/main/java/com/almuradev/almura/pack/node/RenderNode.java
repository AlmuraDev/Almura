/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

public class RenderNode implements INode<Boolean> {

    private final boolean normalCube, opaque;

    public RenderNode(boolean normalCube, boolean opaque) {
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
