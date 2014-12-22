/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

public class GameObjectProperty implements IProperty<Object> {

    private final Object object;
    private final int data;

    public GameObjectProperty(Object object) {
        this(object, 0);
    }

    public GameObjectProperty(Object object, int data) {
        this.object = object;
        this.data = data;
    }

    @Override
    public Object getSource() {
        return object;
    }

    public int getData() {
        return data;
    }
}
