/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.client.model;

import com.almuradev.shared.registry.ResourceLocations;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.CatalogType;

@SideOnly(Side.CLIENT)
public final class ModelResourceLocations {

    private static final String NORMAL = "normal";

    private ModelResourceLocations() {
    }

    public static ModelResourceLocation fromCatalog(final CatalogType catalog) {
        final String string = catalog.getId();
        return new ModelResourceLocation(
                0,
                ResourceLocations.findNamespace(string),
                ResourceLocations.findValue(string),
                NORMAL
        );
    }
}
