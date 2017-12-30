/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj;

public interface OBJModelConfig {

    String COMMENT = "#";

    String FACE = "f";
    String GROUP = "g";
    /**
     * @see Material
     */
    String MATERIAL_LIBRARY = "mtllib";
    String PERSPECTIVE = "pe";
    String ROTATION = "rn";
    String SCALE = "se";
    String TRANSLATION = "tn";
    String USE_MATERIAL = "usemtl";
    String VERTEX = "v";
    String VERTEX_NORMAL = "vn";
    String VERTEX_TEXTURE_COORDINATE = "vt";

    interface Material {

        String DIFFUSE = "map_Kd";
        String NEW_MATERIAL = "newmtl";
    }
}
