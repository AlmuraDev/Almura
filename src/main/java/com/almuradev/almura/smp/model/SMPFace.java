/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.smp.model;

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Vertex;

import java.util.List;

public class SMPFace extends Face {

    private final int textureId;

    public SMPFace(int textureId, Vertex[] vertexes, RenderParameters params) {
        super(vertexes, params);
        this.textureId = textureId;
    }

    public SMPFace(int textureId, Vertex... vertexes) {
        super(vertexes);
        this.textureId = textureId;
    }

    public SMPFace(int textureId, List<Vertex> vertexes) {
        super(vertexes);
        this.textureId = textureId;
    }

    public SMPFace(int textureId, Face face) {
        super(face);
        this.textureId = textureId;
    }

    public SMPFace(int textureId, Face face, RenderParameters params) {
        super(face, params);
        this.textureId = textureId;
    }

    public int getTextureId() {
        return textureId;
    }
}
