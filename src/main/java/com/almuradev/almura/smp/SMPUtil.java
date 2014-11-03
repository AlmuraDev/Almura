package com.almuradev.almura.smp;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.util.IIcon;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SMPUtil {
    public static Map<Integer, List<Integer>> extractCoordsFrom(YamlConfiguration reader) {
        final List<String> textureCoordinatesList = reader.getChild("Coords").getStringList();

        final Map<Integer, List<Integer>> textureCoordinatesByFace = new HashMap<>();

        for (int i = 0; i < textureCoordinatesList.size(); i++) {
            final String[] coordSplit = textureCoordinatesList.get(i).split(" ");

            final List<Integer> coords = new LinkedList<>();
            for (String coord : coordSplit) {
                coords.add(Integer.parseInt(coord));
            }

            textureCoordinatesByFace.put(i, coords);
        }

        return textureCoordinatesByFace;
    }

    public static ClippedIcon[] generateClippedIconsFromCoords(SMPPack pack, IIcon source, String textureName, Map<Integer, List<Integer>> texCoords) {
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(Paths.get(Filesystem.CONFIG_SMPS_PATH.toString(), pack.getName() + ".smp").toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (zipFile == null) {
            return null;
        }

        InputStream textureStream = null;

        try {
            textureStream = zipFile.getInputStream(new ZipEntry(textureName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textureStream == null) {
            return null;
        }

        Dimension dimension = null;

        try {
            dimension = Filesystem.getImageDimension(textureStream);
        } catch (IOException e) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.error("Failed to load texture [" + textureName + "] for dimensions", e);
            }
        } finally {
            try {
                textureStream.close();
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (dimension == null) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.error("Failed to calculate the dimensions for texture [" + textureName + "]");
            }
            return null;
        }

        ClippedIcon[] clippedIcons = new ClippedIcon[texCoords.size()];

        for (int i = 0; i < texCoords.size(); i++) {
            final List<Integer> coordList = texCoords.get(i);

            clippedIcons[i] =
                    new ClippedIcon((MalisisIcon) source, (float) (coordList.get(0) / dimension.getWidth()),
                                    (float) (coordList.get(1) / dimension.getHeight()), (float) (coordList.get(2) / dimension.getWidth()),
                                    (float) (coordList.get(3) / dimension.getHeight()));
        }

        return clippedIcons;
    }
}
