/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import com.almuradev.almura.pack.mapper.GameObject;

public class GameObjectProperty implements IProperty<GameObject> {

    private final GameObject object;

    public GameObjectProperty(GameObject object) {
        this.object = object;
    }

    @Override
    public GameObject getSource() {
        return object;
    }
}
