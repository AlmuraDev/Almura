/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.serializer;

import ninja.leaping.configurate.ConfigurationNode;

public interface ConfigurationNodeDeserializer<T> {

    T deserialize(final ConfigurationNode node);
}
