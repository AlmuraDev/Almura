/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.type;

import com.almuradev.almura.configuration.AbstractConfiguration;
import com.almuradev.almura.configuration.category.ClientCategory;
import com.almuradev.almura.configuration.category.DebugCategory;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ClientConfiguration extends AbstractConfiguration {
    @Setting public final ClientCategory client = new ClientCategory();
    @Setting public final DebugCategory debug = new DebugCategory();
}
