/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.util.IIcon;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PackUtil {

    public static Map<Integer, List<Integer>> parseCoordinatesFrom(List<String> value) throws NumberFormatException {
        final Map<Integer, List<Integer>> textureCoordinatesByFace = Maps.newHashMap();

        for (int i = 0; i < value.size(); i++) {
            textureCoordinatesByFace.put(i, parseStringToNumericList(Integer.class, value.get(i), 4));
        }

        if (textureCoordinatesByFace.isEmpty()) {
            throw new NumberFormatException("No texture coordinates were provided.");
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
            if (Configuration.DEBUG_ALL || Configuration.DEBUG_PACKS) {
                Almura.LOGGER.error("Failed to load texture [" + textureName + "] for dimensions", e);
            }
        }

        if (dimension == null) {
            if (Configuration.DEBUG_ALL || Configuration.DEBUG_PACKS) {
                Almura.LOGGER.error("Failed to calculate the dimensions for texture [" + textureName + "]");
            }
        }

        if (dimension != null) {
            for (int i = 0; i < textureCoordinates.size(); i++) {
                final List<Integer> coordinatesList = textureCoordinates.get(i);

                clippedIcons[i] =
                        new ClippedIcon((MalisisIcon) source, (float) (coordinatesList.get(0) / dimension.getWidth()),
                                (float) (coordinatesList.get(1) / dimension.getHeight()),
                                (float) (coordinatesList.get(2) / dimension.getWidth()),
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

    @SuppressWarnings("unchecked")
    public static <N extends Number> Pair<N, N> getRange(Class<N> clazz, String rawRangeSource, N fallback) throws NumberFormatException {
        N minAmount = fallback, maxAmount = minAmount;
        if (!rawRangeSource.isEmpty()) {
            rawRangeSource = rawRangeSource.trim().endsWith("-") ? rawRangeSource.substring(0, rawRangeSource.length() - 1) : rawRangeSource;
            final String[] split = rawRangeSource.split("-");
            int minIndex = 0, maxIndex = Integer.MIN_VALUE;
            boolean minNeg, maxNeg = minNeg = false;
            if (split[0].startsWith("^")) {
                minNeg = true;
                split[0] = split[0].substring(1, split[0].length());
            }
            if (split.length > 1) {
                maxNeg = split[1].startsWith("^");
                maxIndex = 1;
                if (maxNeg) {
                    split[1] = split[1].substring(1, split[1].length());
                }
            }
            if (clazz == Integer.class) {
                minAmount =
                        (N) ((minNeg) ? new Integer(0 - Math.abs(Integer.parseInt(split[minIndex])))
                                : new Integer(Integer.parseInt(split[minIndex])));
                if (maxIndex != Integer.MIN_VALUE) {
                    maxAmount =
                            (N) ((maxNeg) ? new Integer(0 - Math.abs(Integer.parseInt(split[maxIndex])))
                                    : new Integer(Integer.parseInt(split[maxIndex])));
                } else {
                    maxAmount = minAmount;
                }

                if (maxAmount.intValue() < minAmount.intValue()) {
                    final N temp = minAmount;
                    minAmount = maxAmount;
                    maxAmount = temp;
                }
            } else if (clazz == Double.class) {
                minAmount =
                        (N) ((minNeg) ? new Double(0 - Math.abs(Double.parseDouble(split[minIndex])))
                                : new Double(Double.parseDouble(split[minIndex])));
                if (maxIndex != Integer.MIN_VALUE) {
                    maxAmount =
                            (N) ((maxNeg) ? new Double(0 - Math.abs(Double.parseDouble(split[maxIndex])))
                                    : new Double(Double.parseDouble(split[maxIndex])));
                } else {
                    maxAmount = minAmount;
                }
                if (maxAmount.doubleValue() < minAmount.doubleValue()) {
                    final N temp = minAmount;
                    minAmount = maxAmount;
                    maxAmount = temp;
                }
            } else if (clazz == Float.class) {
                minAmount =
                        (N) ((minNeg) ? new Float(0 - Math.abs(Float.parseFloat(split[minIndex]))) : new Float(Float.parseFloat(split[minIndex])));
                if (maxIndex != Integer.MIN_VALUE) {
                    maxAmount =
                            (N) ((maxNeg) ? new Float(0 - Math.abs(Float.parseFloat(split[maxIndex])))
                                    : new Float(Float.parseFloat(split[maxIndex])));
                } else {
                    maxAmount = minAmount;
                }
                if (maxAmount.floatValue() < minAmount.floatValue()) {
                    final N temp = minAmount;
                    minAmount = maxAmount;
                    maxAmount = temp;
                }
            }
        }
        return new ImmutablePair<>(minAmount, maxAmount);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> List<T> parseStringToNumericList(Class<T> clazz, String raw, int expectedSize) throws NumberFormatException {
        final List<T> parsed = Lists.newLinkedList();
        if (raw.isEmpty()) {
            return parsed;
        }
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

    public static List<String> parseNewlineStringIntoList(String raw) {
        List<String> lines = Lists.newLinkedList();
        Collections.addAll(lines, raw.split("\\n"));
        return lines;
    }

    public static BiomeGenBase getBiome(String biomeSource) {
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (biome.biomeName.equalsIgnoreCase(biomeSource)) {
                return biome;
            }
        }
        return null;
    }
}
