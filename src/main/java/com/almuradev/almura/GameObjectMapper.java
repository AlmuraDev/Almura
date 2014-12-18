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
 *     bonemeal:
 *         dye: 15
 *     dye_blue:
 *         dye: 4
 */
public class GameObjectMapper {
    private static final Map<String, Map<String, Pair<?, ?>>> MAPPED = Maps.newHashMap();

    public static <OBJECT, DATA> OBJECT add(String modid, String remapped, OBJECT key, DATA value) {
        Map<String, Pair<?, ?>> identifierMap = MAPPED.get(modid);
        if (identifierMap == null) {
            identifierMap = Maps.newHashMap();
            MAPPED.put(modid, identifierMap);
        }

        identifierMap.put(remapped, new ImmutablePair<>(key, value));

        return key;
    }

    public static Optional<? extends Pair<?, ?>> get(String modid, String remapped) {
        Map<String, Pair<?, ?>> identifierMap = MAPPED.get(modid);
        if (identifierMap != null) {
            return Optional.fromNullable(identifierMap.get(remapped));
        }
        return Optional.absent();
    }

    public static Map<String, Pair<?, ?>> getAll(String modid) {
        Map<String, Pair<?, ?>> identifierMap = MAPPED.get(modid);
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
            for (String remapped : reader.getNode(modid).getKeys(false)) {
                final ConfigurationNode remappedConfigurationNode = reader.getNode(modid).getNode(remapped);
                final String rawIdentifier = remappedConfigurationNode.getKeys(true).toArray(new String[1])[0];
                final Object found = PackCreator.getGameObject(modid + "\\" + rawIdentifier);
                if (found == null) {
                    Almura.LOGGER.warn("Game object [" + rawIdentifier + "] is neither a block or item within mod [" + modid + "].");
                    continue;
                }
                add(modid, remapped, found, remappedConfigurationNode.getChild(rawIdentifier).getValue());
            }
        }
    }
}
