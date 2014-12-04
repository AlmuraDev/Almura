/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import com.almuradev.almura.Filesystem;
import com.almuradev.almura.pack.ContentPack;
import com.flowpowered.cerealization.config.ConfigurationException;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.renderer.model.loader.ObjFileImporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PackModel extends MalisisModel {

    private final String name;

    public PackModel(String name, InputStream stream) {
        super(new ObjFileImporter(stream));
        this.name = name;
    }

    public static PackModel createFromReader(ContentPack pack, String name) throws ConfigurationException, IOException {
        return new PackModel(name, Files.newInputStream(
                Paths.get(Filesystem.CONFIG_YML_PATH.toString(), pack.getName() + File.separator + name + ".obj")));
    }

    public String getName() {
        return name;
    }
}
