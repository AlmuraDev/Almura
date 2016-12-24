/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class ResourceLocationUtil {

    /**
     * Gets the {@link ResourceLocation} from parsing the provided raw Location.
     *
     * Note: If the fallback domain is null and a domain isn't known, this will default to "minecraft".
     *
     * @param rawLocation The raw location to parse
     * @param fallbackDomain If the raw location has no domain, use this fallback instead
     * @param parentPath If the raw location has no parent, prefix this path if it is not null
     * @return The generated resource rawLocation
     */
    public static ResourceLocation resourceLocationFrom(String rawLocation, String fallbackDomain, String parentPath) {
        final String[] domainAndPath = rawLocation.split(":");
        String domain;
        String path;
        if (domainAndPath.length != 2) {
            if (fallbackDomain == null) {
                domain = "minecraft";
            } else {
                domain = fallbackDomain;
            }

            path = domainAndPath[0];

            if (!path.contains("/") && parentPath != null) {
                path = parentPath + "/" + path;
            }
        } else {
            domain = domainAndPath[0];
            path = domainAndPath[1];

            if (!path.contains("/") && parentPath != null) {
                path = parentPath + "/" + path;
            }
        }

        return new ResourceLocation(domain, path);
    }

    /**
     * Gets the parent {@link ResourceLocation} of the provided location or {@link Optional#empty()} otherwise.
     *
     * @param location The location to get the parent of
     * @return The parent
     */
    public static Optional<ResourceLocation> getParent(ResourceLocation location) {
        final String path = location.getResourcePath();

        final int lastSlashIndex = path.lastIndexOf("/");

        if (lastSlashIndex == -1) {
            return Optional.empty();
        }

        return Optional.of(new ResourceLocation(location.getResourceDomain(), path.substring(0, lastSlashIndex)));
    }

    /**
     * Gets the filename in the {@link ResourceLocation}.
     *
     * Note: The returned String may or may not contain the file extension. Resource locations have no contract that
     * enforces an extension so be sure to check.
     *
     * Note: The returned String may not point to a file. Resource locations don't enforce that the path points to
     * an actual file and could very well point to a folder so be sure to check.
     *
     * @param location The location to get the filename of
     * @return The filename
     */
    public static String getFileName(ResourceLocation location) {
        final String path = location.getResourcePath();

        final int lastSlashIndex = path.lastIndexOf("/");
        if (lastSlashIndex == -1) {
            return path;
        }

        return path.substring(lastSlashIndex + 1, path.length());
    }
}
