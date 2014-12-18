package com.almuradev.almura;

import com.almuradev.almura.pack.PackCreator;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Map;

/**
 * minecraft:
 *     dye:
 *         bonemeal: 15
 */
public class GameObjectMapper {
    private static final Map<String, Map<Object, Pair<String, Object>>> MAPPED = Maps.newHashMap();

    public static void add(String modid, String remapped, Object parent, Object value) {
        Map<Object, Pair<String, Object>> identifierMap = MAPPED.get(modid);
        if (identifierMap == null) {
            identifierMap = Maps.newHashMap();
            MAPPED.put(modid, identifierMap);
        }

        identifierMap.put(parent, new ImmutablePair<>(remapped, value));
    }

    public static Optional<? extends Pair<String, Object>> get(String modid, String remapped) {
        Map<Object, Pair<String, Object>> identifierMap = MAPPED.get(modid);
        if (identifierMap != null) {
            return Optional.fromNullable(identifierMap.get(remapped));
        }
        return Optional.absent();
    }

    public static Map<Object, Pair<String, Object>> getAll(String modid) {
        Map<Object, Pair<String, Object>> identifierMap = MAPPED.get(modid);
        if (identifierMap == null) {
            identifierMap = Maps.newHashMap();
            MAPPED.put(modid, identifierMap);
        }
        return Collections.unmodifiableMap(identifierMap);
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
                final Object found = PackCreator.getGameObject(modid + "\\" + rawObjectIdentifier);
                if (found == null) {
                    Almura.LOGGER.warn("Game object [" + rawObjectIdentifier + "] is neither a block or item within mod [" + modid + "].");
                    continue;
                }

                final ConfigurationNode objectConfigurationNode = modidConfigurationNode.getNode(rawObjectIdentifier);
                for (String remapped : objectConfigurationNode.getKeys(false)) {
                    add(modid, remapped, found, objectConfigurationNode.getChild(remapped).getValue());
                }
            }
        }

        System.out.println(MAPPED);
    }
}
