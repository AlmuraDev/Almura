/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.ContentPack;
import com.flowpowered.cerealization.config.ConfigurationException;
import net.malisis.core.renderer.model.MalisisModel;
import net.minecraft.util.ResourceLocation;

public class PackModel extends MalisisModel {
    private static final PackObjFileImporter IMPORTER = new PackObjFileImporter();
    private final String name;

    public PackModel(String name, ResourceLocation location) {
        super(location, IMPORTER);
        this.name = name;
    }

    public static PackModel createFromReader(ContentPack pack, String name) throws ConfigurationException {
        return new PackModel(name, new ResourceLocation(Almura.MOD_ID, "packs/" + pack.getName() + "//" + name + ".obj"));
    }

    public String getName() {
        return name;
    }
}
