/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.mapper;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.registry.GameRegistry;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.util.Map;

/**
 * minecraft:
 * dye:
 * bonemeal: 15
 */
public class GameObjectMapper {

    private static final Map<String, Map<String, GameObject>> MAPPED = Maps.newHashMap();

    public static boolean add(String modid, Object gameObject, String name, Object value) {
        Map<String, GameObject> identifierMap = MAPPED.get(modid);
        if (identifierMap == null) {
            identifierMap = Maps.newHashMap();
            MAPPED.put(modid, identifierMap);
        }
        for (Map.Entry<String, Map<String, GameObject>> modEntry : MAPPED.entrySet()) {
            for (Map.Entry<String, GameObject> objectEntry : modEntry.getValue().entrySet()) {
                if (objectEntry.getKey().equalsIgnoreCase(name)) {
                    Almura.LOGGER.warn("Duplicate [" + name + "] for Minecraft object [" + gameObject + "] for mod [" + modEntry.getKey()
                            + "]. Only one remapped name is allowed!");
                    return false;
                }
            }
        }
        identifierMap.put(name, new GameObject(modid, gameObject, name, (Integer) value));
        return true;
    }

    public static Optional<GameObject> get(String modid, String identifier) {
        final Map<String, GameObject> identifierMap = MAPPED.get(modid);
        if (identifierMap != null) {
            for (Map.Entry<String, GameObject> entry : identifierMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(identifier)) {
                    return Optional.of(entry.getValue());
                }
            }
        }
        return Optional.absent();
    }

    public static void load() throws IOException {
        final ConfigurationNode reader = YAMLConfigurationLoader.builder().setFile(Filesystem.CONFIG_MAPPINGS_PATH.toFile()).setFlowStyle(
                DumperOptions.FlowStyle.BLOCK).build().load();

        for (Map.Entry<Object, ? extends ConfigurationNode> modidEntry : reader.getChildrenMap().entrySet()) {
            final String modid = (String) modidEntry.getKey();
            for (Map.Entry<Object, ? extends ConfigurationNode> rawObjectEntry : modidEntry.getValue().getChildrenMap().entrySet()) {
                final String rawObjectIdentifier = (String) rawObjectEntry.getKey();
                final Optional<GameObject> found = getGameObject(modid + "\\" + rawObjectIdentifier, false);
                if (!found.isPresent()) {
                    Almura.LOGGER.warn("Object [" + rawObjectIdentifier + "] is neither a block or item registered to mod [" + modid + "].");
                    continue;
                }
                for (Map.Entry<Object, ? extends ConfigurationNode> remappedEntry : rawObjectEntry.getValue().getChildrenMap().entrySet()) {
                    final String remapped = (String) remappedEntry.getKey();
                    final int value = remappedEntry.getValue().getInt(0);
                    if (add(modid, found.get().minecraftObject, remapped, value) && (Configuration.DEBUG_ALL
                            || Configuration.DEBUG_MAPPINGS)) {
                        Almura.LOGGER
                                .info("Registered mapping [" + remapped + "] with value [" + value + "] for object [" + found.get().minecraftObject
                                        + "] for mod [" + modid
                                        + "].");
                    }
                }
            }
        }
    }

    private static Optional<Object> getMinecraftObject(String modid, String identifier, boolean itemFirst) {
        Object object;

        if (itemFirst) {
            object = GameRegistry.findItem(modid, identifier);

            if (object == null) {
                object = GameRegistry.findBlock(modid, identifier);
            }
        } else {
            object = GameRegistry.findBlock(modid, identifier);

            if (object == null) {
                object = GameRegistry.findItem(modid, identifier);
            }
        }
        return Optional.fromNullable(object);
    }

    public static Optional<GameObject> getGameObject(String rawSource, boolean itemFirst) {
        final Pair<String, String> parsedModidIdentifier = parseModidIdentifierFrom(rawSource);
        return getGameObject(parsedModidIdentifier.getKey(), parsedModidIdentifier.getValue(), itemFirst);
    }

    public static Optional<GameObject> getGameObject(String modid, String identifier, boolean itemFirst) {
        final Optional<Object> object = getMinecraftObject(modid, identifier, itemFirst);
        if (!object.isPresent()) {
            return get(modid, identifier);
        }

        return Optional.of(new GameObject(modid, object.get(), "", 0));
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
}
