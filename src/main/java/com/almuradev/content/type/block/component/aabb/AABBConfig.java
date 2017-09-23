/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.component.aabb;

public interface AABBConfig {

    String COLLISION = "collision";
    String SHAPE = "shape";
    String WIREFRAME = "wireframe";

    interface Box {

        String MIN = "min";
        String MAX = "max";
    }
}
