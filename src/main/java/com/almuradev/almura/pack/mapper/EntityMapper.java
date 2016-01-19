/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.mapper;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.util.FileSystem;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * minecraft:
 * Player: player
 * Zombie: zombie
 */
public class EntityMapper {

    private static final Map<String, Set<Pair<String, String>>> MAPPED = Maps.newHashMap();

    public static boolean add(String modid, String name, String remapped) {
        Set<Pair<String, String>> identifierSet = MAPPED.get(modid);
        if (identifierSet == null) {
            identifierSet = Sets.newHashSet();
            MAPPED.put(modid, identifierSet);
        }

        for (Pair<String, String> identifierEntry : identifierSet) {
            if (identifierEntry.getValue().equalsIgnoreCase(remapped)) {
                return false;
            }
        }

        return identifierSet.add(new ImmutablePair<>(name, remapped));
    }

    public static Optional<String> get(String modid, String identifier) {
        final Set<Pair<String, String>> identifierSet = MAPPED.get(modid);
        if (identifierSet != null) {
            for (Pair<String, String> value : identifierSet) {
                if (value.getKey().equalsIgnoreCase(identifier)) {
                    return Optional.of(value.getValue());
                }
            }
        }
        return Optional.absent();
    }

    public static Optional<String> getByRemapped(String modid, String remapped) {
        final Set<Pair<String, String>> modidSet = MAPPED.get(modid);
        for (Pair<String, String> identifierEntry : modidSet) {
            if (identifierEntry.getValue().equals(remapped)) {
                return Optional.of(identifierEntry.getKey());
            }
        }
        return Optional.absent();
    }

    public static Optional<Class<? extends Entity>> getEntityClass(String rawSource) {
        final Pair<String, String> parsedModidIdentifier = GameObjectMapper.parseModidIdentifierFrom(rawSource);
        return getEntityClass(parsedModidIdentifier.getKey(), parsedModidIdentifier.getValue());
    }

    @SuppressWarnings("unchecked")
    public static Optional<Class<? extends Entity>> getEntityClass(String modid, String identifier) {
        final boolean minecraft = "minecraft".equalsIgnoreCase(modid);

        if (minecraft && identifier.equalsIgnoreCase("Player")) {
            return Optional.<Class<? extends Entity>>of(EntityPlayer.class);
        }

        Class<? extends Entity> entityClazz = (Class<? extends Entity>) EntityList.stringToClassMapping.get(
                minecraft ? identifier : modid + "." + identifier);
        if (entityClazz == null) {
            final Optional<String> value = get(modid, identifier);
            if (value.isPresent()) {
                entityClazz = (Class<? extends Entity>) EntityList.stringToClassMapping.get(minecraft ? value : modid + "." + value);
            }
        }

        return Optional.<Class<? extends Entity>>fromNullable(entityClazz);
    }

    @SuppressWarnings("unchecked")
    public static Optional<Class<? extends Entity>> getEntityClassRemapped(String modid, String remapped) {
        final boolean minecraft = modid.equalsIgnoreCase("minecraft");
        final Optional<String> value = getByRemapped(modid, remapped);
        Class<? extends Entity> entityClazz = null;

        if (value.isPresent()) {
            if (minecraft && value.get().equalsIgnoreCase("Player")) {
                entityClazz = EntityPlayer.class;
            } else {
                entityClazz = (Class<? extends Entity>) EntityList.stringToClassMapping.get(minecraft ? value.get() : modid + "." + value.get());
            }
        }

        return Optional.<Class<? extends Entity>>fromNullable(entityClazz);
    }

    public static void load() throws IOException {
        final ConfigurationNode root = YAMLConfigurationLoader.builder().setFile(FileSystem.CONFIG_ENTITY_MAPPINGS_PATH.toFile()).build().load();

        for (Map.Entry<Object, ? extends ConfigurationNode> modidEntry : root.getChildrenMap().entrySet()) {
            final String modid = (String) modidEntry.getKey();

            for (Map.Entry<Object, ? extends ConfigurationNode> identifierEntry : modidEntry.getValue().getChildrenMap().entrySet()) {
                final String identifier = (String) identifierEntry.getKey();
                Optional<Class<? extends Entity>> exists;

                if ("minecraft".equalsIgnoreCase(modid) && identifier.equalsIgnoreCase("Player")) {
                    exists = Optional.<Class<? extends Entity>>of(EntityPlayer.class);
                } else {
                    exists = getEntityClass(modid, identifier);
                }

                if (!exists.isPresent()) {
                    Almura.LOGGER.warn("Identifier [" + identifier + "] is not a registered entity for mod [" + modid + "].");
                }

                final String remapped = identifierEntry.getValue().getString();

                if (add(modid, identifier, remapped) && (Configuration.DEBUG_ALL || Configuration.DEBUG_MAPPINGS)) {
                    Almura.LOGGER.info("Registered entity mapping [" + remapped + "] to [" + identifier + "] for mod [" + modid + "].");
                }
            }
        }
    }
}
