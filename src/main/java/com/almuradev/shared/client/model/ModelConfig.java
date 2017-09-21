/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.client.model;

public interface ModelConfig {

    String COMMENT = "#";

    interface OBJ {

        String MATERIAL_LIBRARY = "mtllib";
        String USE_MATERIAL = "usemtl";
        String VERTEX = "v";
        String VERTEX_NORMAL = "vn";
        String VERTEX_TEXTURE_COORDINATE = "vt";
        String GROUP = "g";
        String FACE = "f";
        String PERSPECTIVE = "pe";
        String TRANSLATION = "tn";
        String ROTATION = "rn";
        String SCALE = "se";

        interface Material {

            String NEW_MATERIAL = "newmtl";
            String DIFFUSE = "map_Kd";
        }
    }
}
