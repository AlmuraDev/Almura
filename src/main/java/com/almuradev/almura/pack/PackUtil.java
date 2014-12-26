/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.registry.GameRegistry;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.util.IIcon;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class PackUtil {

    public static Map<Integer, List<Integer>> parseCoordinatesFrom(List<String> value) throws NumberFormatException {
        final Map<Integer, List<Integer>> textureCoordinatesByFace = Maps.newHashMap();

        for (int i = 0; i < value.size(); i++) {
            textureCoordinatesByFace.put(i, parseStringToNumericList(Integer.class, value.get(i), 4));
        }

        return textureCoordinatesByFace;
    }

    public static ClippedIcon[] generateClippedIconsFromCoordinates(IIcon source, String textureName,
                                                                    Map<Integer, List<Integer>> textureCoordinates) {
        final ClippedIcon[] clippedIcons = new ClippedIcon[textureCoordinates.size()];
        Dimension dimension = null;

        try {
            dimension =
                    Filesystem.getImageDimension(Files.newInputStream(
                            Paths.get(Filesystem.CONFIG_IMAGES_PATH.toString(), textureName + ".png")));
        } catch (IOException e) {
            if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                Almura.LOGGER.error("Failed to load texture [" + textureName + "] for dimensions", e);
            }
        }

        if (dimension == null) {
            if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                Almura.LOGGER.error("Failed to calculate the dimensions for texture [" + textureName + "]");
            }
        }

        if (dimension != null) {
            for (int i = 0; i < textureCoordinates.size(); i++) {
                final List<Integer> coordinatesList = textureCoordinates.get(i);

                clippedIcons[i] =
                        new ClippedIcon((MalisisIcon) source, (float) (coordinatesList.get(0) / dimension.getWidth()),
                                        (float) (coordinatesList.get(1) / dimension.getHeight()), (float) (coordinatesList.get(2) / dimension.getWidth()),
                                        (float) (coordinatesList.get(3) / dimension.getHeight()));
            }
        }
        return clippedIcons;
    }

    public static boolean isEmptyClip(ClippedIcon[] value) {
        boolean isEmpty = true;

        if (value != null) {
            for (ClippedIcon icon : value) {
                if (icon != null) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public static Object getGameObject(String rawSource) {
        final Pair<String, String> parsedModidIdentifier = parseModidIdentifierFrom(rawSource);
        return getGameObject(parsedModidIdentifier.getKey(), parsedModidIdentifier.getValue());
    }

    public static Object getGameObject(String modid, String identifier) {
        Object object = GameRegistry.findBlock(modid, identifier);
        if (object == null) {
            object = GameRegistry.findItem(modid, identifier);
            if (object == null) {
                final Optional<GameObjectMapper.TrioWrapper<Object, String, Object>> wrapper = GameObjectMapper.get(modid, identifier);
                if (wrapper.isPresent()) {
                    object = wrapper.get().object;
                }
            }
        }
        return object;
    }

    public static Pair<String, String> parseModidIdentifierFrom(String rawSource) {
        final String[] separated = rawSource.split(StringEscapeUtils.escapeJava("\\"));
        String modid = separated[0].toLowerCase();
        String identifier;
        if (separated.length > 1) {
            identifier = rawSource.split(modid + StringEscapeUtils.escapeJava("\\"))[1];
        } else {
            identifier = modid;
        }
        if (identifier.equalsIgnoreCase(modid)) {
            identifier = identifier.toLowerCase();
            modid = "minecraft";
        }

        return new ImmutablePair<>(modid, identifier);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number> Pair<N, N> getRange(Class<N> clazz, String rawRangeSource, N minimum) throws NumberFormatException {
        N minAmount = minimum, maxAmount = minAmount;
        if (!rawRangeSource.isEmpty()) {
            final String[] split = rawRangeSource.split("-");
            if (clazz == Integer.class) {
                minAmount = (N) new Integer(Integer.parseInt(split[0]));
                if (split.length > 1) {
                    maxAmount = (N) new Integer(Integer.parseInt(split[1]));
                } else {
                    maxAmount = minAmount;
                }
            } else if (clazz == Double.class) {
                minAmount = (N) new Double(Double.parseDouble(split[0]));
                if (split.length > 1) {
                    maxAmount = (N) new Double(Double.parseDouble(split[1]));
                } else {
                    maxAmount = minAmount;
                }
            } else if (clazz == Float.class) {
                minAmount = (N) new Float(Float.parseFloat(split[0]));
                if (split.length > 1) {
                    maxAmount = (N) new Float(Float.parseFloat(split[0]));
                } else {
                    maxAmount = minAmount;
                }
            }
        }
        return new ImmutablePair<>(minAmount, maxAmount);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> List<T> parseStringToNumericList(Class<T> clazz, String raw, int expectedSize) throws NumberFormatException {
        final List<T> parsed = Lists.newLinkedList();
        final String[] split = raw.split(" ");
        if (expectedSize != Integer.MIN_VALUE && split.length != expectedSize) {
            throw new NumberFormatException("Expected size [" + expectedSize + "] but actual size was [" + split.length + "]");
        }
        for (String coordinate : raw.split(" ")) {
            Object object = null;
            if (clazz == Float.class) {
                object = Float.parseFloat(coordinate);
            } else if (clazz == Integer.class) {
                object = Integer.parseInt(coordinate);
            } else if (clazz == Double.class) {
                object = Double.parseDouble(coordinate);
            }

            if (object != null) {
                parsed.add((T) object);
            }
        }
        return parsed;
    }
}
