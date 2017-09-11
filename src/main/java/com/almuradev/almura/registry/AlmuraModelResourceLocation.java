/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import org.spongepowered.api.CatalogType;

public class AlmuraModelResourceLocation extends ModelResourceLocation {

    public AlmuraModelResourceLocation(final CatalogType catalog) {
        super(0, parseString(catalog.getId()));
    }

    private static String[] parseString(String string) {
        final String value;
        final int index = string.indexOf('/');
        if (index != -1) {
            value = string.substring(index, string.length());
        } else {
            value = string;
        }
        return new String[]{ResourceLocations.findNamespace(string), value, "normal"};
    }
}
