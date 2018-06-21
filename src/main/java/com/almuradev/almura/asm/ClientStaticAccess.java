/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm;

import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ClientStaticAccess {

    /*
     *  _____   ____    _   _  ____ _______   _    _  _____ ______
     * |  __ \ / __ \  | \ | |/ __ \__   __| | |  | |/ ____|  ____|
     * | |  | | |  | | |  \| | |  | | | |    | |  | | (___ | |__
     * | |  | | |  | | | . ` | |  | | | |    | |  | |\___ \|  __|
     * | |__| | |__| | | |\  | |__| | | |    | |__| |____) | |____
     * |_____/ \____/  |_| \_|\____/  |_|     \____/|_____/|______|
     *
     * really, don't.
     */
    @Inject public static MappedConfiguration<ClientConfiguration> configAdapter;
    @Inject public static Logger logger;
}
