/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.handbook.client;

import com.almuradev.almura.feature.handbook.network.ClientboundHandbook;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;

/**
 * The Skills Handbook on the client: opened with what the server sent.
 */
public final class ClientHandbook {

    private ClientHandbook() {
    }

    public static void open(final ClientboundHandbook handbook) {
        InGameScreens.open(new HandbookScreen(handbook), Minecraft.getInstance().gui.screen());
    }
}
