/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Vertex;

import java.util.List;

public class PackMirrorFace extends PackFace {

    public PackMirrorFace(int textureId, Vertex[] vertexes, RenderParameters params) {
        super(textureId, vertexes, params);
    }

    public PackMirrorFace(int textureId, Vertex... vertexes) {
        super(textureId, vertexes);
    }

    public PackMirrorFace(int textureId, List<Vertex> vertexes) {
        super(textureId, vertexes);
    }

    public PackMirrorFace(PackFace face) {
        super(face);
    }

    public PackMirrorFace(PackFace face, RenderParameters params) {
        super(face, params);
    }
}
