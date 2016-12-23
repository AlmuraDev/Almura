package com.almuradev.almura;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    public static final class Plugin {
        public static final String ID = "almura";
        public static final String PROXY_CLIENT_CLASSPATH = "com.almuradev.almura.client.ClientProxy";
        public static final String PROXY_SERVER_CLASSPATH = "com.almuradev.almura.server.ServerProxy";
    }

    public static final class FileSystem {
        public static final Path PATH_CONFIG = Paths.get(".").resolve("config").resolve(Plugin.ID);
        public static final Path PATH_CONFIG_CLIENT = PATH_CONFIG.resolve("client.conf");
        public static final Path PATH_CONFIG_PACKS = PATH_CONFIG.resolve("packs");

        public static void construct() {
            if (Files.notExists(PATH_CONFIG_PACKS)) {
                try {
                    Files.createDirectories(PATH_CONFIG_PACKS);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create [" + PATH_CONFIG_PACKS + "]!", e);
                }
            }
        }
    }

    public static final class Config {
        public static final String HEADER = "2.0\nAlmura configuration\n\nFor further assistance, join #almura on EsperNet.";
    }

    @SideOnly(Side.CLIENT)
    public static final class Gui {

    }
}
