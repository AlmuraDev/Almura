/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node;

public abstract class ToggleableNode<T> implements INode<T> {
    private final boolean isEnabled;

    public ToggleableNode(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public final boolean isEnabled() {
        return isEnabled;
    }
}
