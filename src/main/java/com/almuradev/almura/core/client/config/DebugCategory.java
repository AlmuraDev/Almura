/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
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
