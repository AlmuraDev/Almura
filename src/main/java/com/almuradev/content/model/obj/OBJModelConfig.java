/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj;

/*
 * Reference Materials
 *     http://paulbourke.net/dataformats/mtl/
 *     http://people.sc.fsu.edu/~jburkardt/data/mtl/mtl.html
 */
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

    /*
     * Additional features from 3ds
     */

    /**
     * The ambient reflectivity using RGB values.
     */
    String AMBIENT_REFLECTIVITY = "Ka";
    /**
     * The diffuse reflectivity using RGB values.
     */
    String DIFFUSE_REFLECTIVITY = "Kd";
    /**
     * The amount this material dissolves into the background.
     *
     * <p>A factor of {@code 0.0} is fully dissolved (completely transparent).</p>
     */
    String FACTOR = "d";
    /**
     * The illumination model to use in the material.
     */
    String ILLUMINIATION_MODEL = "illum";
    /**
     * The optical density for the surface.
     */
    String OPTICAL_DENSITY = "Ni";
    /**
     * The specular exponent for the current material.
     *
     * <p>This defines the focus of the specular highlight.</p>
     */
    String SPECULAR_EXPONENT = "Ns";
    /**
     * The specular reflectivity using RGB values.
     */
    String SPECULAR_REFLECTIVITY = "Ks";
    /**
     * Transparency value set in Alfa.
     */
    String TRANSPARENCY_ALPHA = "Tr";
    /**
     * The transmission filter using RGB values.
     */
    String TRANSMISSION_FILTER = "Tf";
    /**
     * Unknown value.
     */
    String UNKNOWN_VALUE = "Ke";
}
