/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.serializer;

import com.almuradev.almura.creativetab.CreativeTab;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;

public final class CreativeTabSerializer implements TypeSerializer<CreativeTab> {

    @Override
    public CreativeTab deserialize(TypeToken<?> typeToken, ConfigurationNode configurationNode) throws ObjectMappingException {
        return Sponge.getRegistry().getType(CreativeTab.class, (String) configurationNode.getValue()).orElse(null);
    }

    @Override
    public void serialize(TypeToken<?> typeToken, CreativeTab creativeTab, ConfigurationNode configurationNode)
            throws ObjectMappingException {
        configurationNode.setValue(creativeTab.getName());
    }
}
