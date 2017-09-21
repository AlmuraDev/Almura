/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.client.config;

import com.almuradev.shared.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@SideOnly(Side.CLIENT)
@ConfigSerializable
public class ClientConfiguration implements Configuration {

    @Setting public final ClientCategory client = new ClientCategory();
    @Setting public final DebugCategory debug = new DebugCategory();
}
