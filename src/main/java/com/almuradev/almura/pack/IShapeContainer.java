/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import net.malisis.core.renderer.element.Shape;

public interface IShapeContainer extends IPackObject {

    Shape getShape();

    void setShapeFromPack();
}
