/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
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
