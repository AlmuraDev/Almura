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

    // Reference Materials:  http://paulbourke.net/dataformats/mtl/  && http://people.sc.fsu.edu/~jburkardt/data/mtl/mtl.html

    // Additional features of a MTL file from 3ds
    String SpecularExponent = "Ns";       // Specifies the specular exponent for the current material.  This defines the focus of the specular highlight.
    String OpticalDensity = "Ni";         // Specifies the optical density for the surface.
    String Factor = "d";                  //"factor" is the amount this material dissolves into the background.  A factor of 0.0 is fully dissolved (completely transparent).
    String TransparencyAlpha = "Tr";      // Transparency value set in Alfa.
    String TransmissionFilter = "Tf";     // The Tf statement specifies the transmission filter using RGB values.
    String IlluminiationModel = "illum";  // The "illum" statement specifies the illumination model to use in the material.
    String AmbientReflectivity = "Ka";    // The Ka statement specifies the ambient reflectivity using RGB values.
    String SpecularReflectivity = "Ks";   // The Ks statement specifies the specular reflectivity using RGB values.
    String DiffuseReflectivity = "Kd";    // The Kd statement specifies the diffuse reflectivity using RGB values.
    String UnknownValue = "Ke";           // Unknown Value.

    interface Material {

        String DIFFUSE = "map_Kd";
        String NEW_MATERIAL = "newmtl";
    }
}
