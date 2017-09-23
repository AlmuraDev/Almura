/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.content.AssetConfig;
import org.apache.commons.lang3.text.WordUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    public interface Plugin {

        String ID = "almura";
        String NAME = WordUtils.capitalize(ID);
        String PROXY_CLIENT_CLASSPATH = "com.almuradev.almura.core.client.ClientProxy";
        String PROXY_SERVER_CLASSPATH = "com.almuradev.almura.core.server.ServerProxy";
    }

    public interface FileSystem {

        Path PATH_ROOT = Paths.get(".");

        Path PATH_ASSETS = PATH_ROOT.resolve("assets");
        Path PATH_ASSETS_ALMURA = PATH_ASSETS.resolve(Plugin.ID);
        Path PATH_ASSETS_ALMURA_30 = PATH_ASSETS_ALMURA.resolve(AssetConfig.VERSION_3_0);
        Path PATH_ASSETS_ALMURA_30_PACKS = PATH_ASSETS_ALMURA_30.resolve("packs");

        Path PATH_CONFIG = PATH_ROOT.resolve("config");
        Path PATH_CONFIG_ALMURA = PATH_CONFIG.resolve(Plugin.ID);

        String CONFIG_CLIENT_NAME = "client.conf";
    }

    public interface Config {

        String HEADER = "2.0\nAlmura configuration\n\nFor further assistance, join #almura on EsperNet.";

    }

}
