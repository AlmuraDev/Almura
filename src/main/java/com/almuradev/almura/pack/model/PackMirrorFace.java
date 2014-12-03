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

    public PackMirrorFace(int textureId, Face face) {
        super(textureId, face);
    }

    public PackMirrorFace(int textureId, Face face, RenderParameters params) {
        super(textureId, face, params);
    }
}
