/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.client.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public final class DebugCategory {

    @Setting public boolean all = false;
    @Setting public boolean languages = false;
    @Setting public boolean packs = false;
    @Setting public boolean mappings = false;
    @Setting public boolean recipes = false;
}
