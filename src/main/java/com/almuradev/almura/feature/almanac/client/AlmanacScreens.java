/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.client;

import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacField;
import com.almuradev.almura.feature.menu.InGameScreens;

/**
 * Opens the Farmer's Almanac's windows from the server's messages, over the game (without pausing it), as Almura's other in-game windows.
 * Client only.
 */
public final class AlmanacScreens {

    private AlmanacScreens() {
    }

    public static void openField(final ClientboundAlmanacField field) {
        InGameScreens.open(new AlmanacFieldScreen(field), null);
    }

    public static void openCrops(final ClientboundAlmanacCrops crops) {
        InGameScreens.open(new AlmanacCropsScreen(crops), null);
    }
}
