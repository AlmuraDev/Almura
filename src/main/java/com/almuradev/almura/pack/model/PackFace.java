/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Vertex;

import java.util.List;

public class PackFace extends Face {

    private final int textureId;

    public PackFace(int textureId, Vertex[] vertexes, RenderParameters params) {
        super(vertexes, params);
        this.textureId = textureId;
    }

    public PackFace(int textureId, Vertex... vertexes) {
        super(vertexes);
        this.textureId = textureId;
    }

    public PackFace(int textureId, List<Vertex> vertexes) {
        super(vertexes);
        this.textureId = textureId;
    }

    public PackFace(PackFace face) {
        super(face);
        this.textureId = face.getTextureId();
    }

    public PackFace(PackFace face, RenderParameters params) {
        super(face, params);
        this.textureId = face.getTextureId();
    }

    public int getTextureId() {
        return textureId;
    }
}
