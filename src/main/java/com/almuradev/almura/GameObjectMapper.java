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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * minecraft:
 *     dye:
 *         bonemeal: 15
 */
public class GameObjectMapper {
    private static final Map<String, Map<Object, Set<Pair<String, Object>>>> MAPPED = Maps.newHashMap();

    public static boolean add(String modid, Object gameObject, String name, Object value) {
        Map<Object, Set<Pair<String, Object>>> identifierMap = MAPPED.get(modid);
        if (identifierMap == null) {
            identifierMap = Maps.newHashMap();
            MAPPED.put(modid, identifierMap);
        }
        Set<Pair<String, Object>> pairSet = identifierMap.get(gameObject);
        if (pairSet == null) {
            pairSet = new HashSet<>();
            identifierMap.put(gameObject, pairSet);
        }
        boolean exists = false;
        for (Pair<String, Object> pair : pairSet) {
            if (pair.getKey().equals(name)) {
                Almura.LOGGER.warn("Duplicate remapped name [" + name + "] within mappings.yml. Attempt made to register under object [" + gameObject + "]. Only one remapped name is allowed!");
                exists = true;
            }
        }
        if (!exists) {
            pairSet.add(new ImmutablePair<>(name, value));
        }
        return !exists;
    }

    public static Optional<TrioWrapper<Object, String, Object>> get(String modid, String name) {
        final Map<Object, Set<Pair<String, Object>>> identifierMap = MAPPED.get(modid);
        if (identifierMap != null) {
            for (Map.Entry<Object, Set<Pair<String, Object>>> entry : identifierMap.entrySet()) {
                for (Pair<String, Object> pair : entry.getValue()) {
                    if (pair.getKey().equals(name)) {
                        return Optional.of(new TrioWrapper<>(entry.getKey(), pair));
                    }
                }
            }
        }
        return Optional.absent();
    }

    public static Optional<Map<Object, Set<Pair<String, Object>>>> getAll(String modid) {
        Map<Object, Set<Pair<String, Object>>> identifierMap = MAPPED.get(modid);
        if (identifierMap != null) {
            return Optional.of(Collections.unmodifiableMap(identifierMap));
        }
        return Optional.of(Collections.unmodifiableMap(Collections.<Object, Set<Pair<String, Object>>>emptyMap()));
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
                    if (add(modid, found, remapped, objectConfigurationNode.getChild(remapped).getValue()) && (Configuration.DEBUG_MODE || Configuration.DEBUG_MAPPINGS_MODE)) {
                        Almura.LOGGER.info("Registered mapping [" + remapped + "] for object [" + found + "] for mod [" + modid + "].");
                    }
                }
            }
        }
    }

    public static class TrioWrapper<S, T, U> {
        public final S object;
        public final Pair<T, U> pair;

        public TrioWrapper(S object, Pair<T, U> pair) {
            this.object = object;
            this.pair = pair;
        }
    }
}
