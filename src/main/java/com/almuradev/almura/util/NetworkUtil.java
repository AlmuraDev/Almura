/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

import com.almuradev.almura.Almura;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

public class NetworkUtil {

    public static void sendWorldHUDData(Player player, Transform<World> toTransform) {
        String clientWorldName = toTransform.getExtent().getName();

        switch (clientWorldName) {
            case "DIM-1":
                clientWorldName = "The Nether";
                break;
            case "DIM1":
                clientWorldName = "The End";
                break;
            case "world":
                clientWorldName = "Dakara";
                break;
            case "cemaria":
                clientWorldName = "Cemaria";
                break;
            case "asgard":
                clientWorldName = "Asgard";
                break;
            case "atlantis":
                clientWorldName = "Atlantis";
                break;
            case "othala":
                clientWorldName = "Othala";
                break;
            case "keystone":
                clientWorldName = "Keystone";
                break;
        }
        Almura.proxy.getNetwork().sendTo(player, new SWorldInformationMessage(clientWorldName));
    }
}
