/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.property;

public abstract class ToggleableProperty<T> implements IProperty<T> {
    private final boolean isEnabled;

    public ToggleableProperty(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public final boolean isEnabled() {
        return isEnabled;
    }
}
