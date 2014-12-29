/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.registry.GameRegistry;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * minecraft:
 *     dye:
 *         bonemeal: 15
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

    public static void load() {
        final YamlConfiguration reader = new YamlConfiguration(Filesystem.CONFIG_MAPPINGS_PATH.toFile());
        try {
            reader.load();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        for (String modid : reader.getKeys(false)) {
            final ConfigurationNode modidConfigurationNode = reader.getNode(modid);
            for (String rawObjectIdentifier : modidConfigurationNode.getKeys(false)) {
                final Optional<GameObject> found = getGameObject(modid + "\\" + rawObjectIdentifier);
                if (!found.isPresent()) {
                    Almura.LOGGER.warn("Object [" + rawObjectIdentifier + "] is neither a block or item within mod [" + modid + "].");
                    continue;
                }

                final ConfigurationNode objectConfigurationNode = modidConfigurationNode.getNode(rawObjectIdentifier);
                for (String remapped : objectConfigurationNode.getKeys(false)) {
                    final Object value = objectConfigurationNode.getChild(remapped).getValue();
                    if (add(modid, found.get().minecraftObject, remapped, value) && (Configuration.DEBUG_MODE
                                                               || Configuration.DEBUG_MAPPINGS_MODE)) {
                        Almura.LOGGER
                                .info("Registered mapping [" + remapped + "] with value [" + value + "] for object [" + found.get().minecraftObject + "] for mod [" + modid
                                      + "].");
                    }
                }
            }
        }
    }

    private static Optional<Object> getMinecraftObject(String modid, String identifier) {
        Object object = GameRegistry.findBlock(modid, identifier);
        if (object == null) {
            object = GameRegistry.findItem(modid, identifier);
        }
        return Optional.fromNullable(object);
    }

    public static Optional<GameObject> getGameObject(String rawSource) {
        final Pair<String, String> parsedModidIdentifier = parseModidIdentifierFrom(rawSource);
        return getGameObject(parsedModidIdentifier.getKey(), parsedModidIdentifier.getValue());
    }

    public static Optional<GameObject> getGameObject(String modid, String identifier) {
        final Optional<Object> object = getMinecraftObject(modid, identifier);
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
