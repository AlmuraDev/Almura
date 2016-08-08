/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.serializer;

import com.almuradev.almura.api.CreativeTab;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public final class CreativeTabSerializer implements TypeSerializer<CreativeTab> {

    @Override
    public CreativeTab deserialize(TypeToken<?> typeToken, ConfigurationNode configurationNode) throws ObjectMappingException {
//        final Optional<CreativeTab> optCreativeTab = Sponge.getRegistry().getType(CreativeTab.class, (String) configurationNode.getValue());
//        if (optCreativeTab.isPresent()) {
//            return optCreativeTab.get();
//        }
        return (CreativeTab) net.minecraft.creativetab.CreativeTabs.MISC;
    }

    @Override
    public void serialize(TypeToken<?> typeToken, CreativeTab creativeTab, ConfigurationNode configurationNode)
            throws ObjectMappingException {
        configurationNode.setValue("misc");
    }
}
