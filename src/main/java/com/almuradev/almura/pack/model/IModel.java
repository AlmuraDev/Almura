/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Vertex;

import java.util.List;

public interface IModel {

    Face[] getFaces();

    List<Vertex> getVertexes(Face fe);

    void rotate(float angle, float x, float y, float z);

    void scale(float factor);
}
