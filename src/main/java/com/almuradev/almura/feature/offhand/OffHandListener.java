/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.offhand;

import com.almuradev.almura.shared.event.Witness;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;

public class OffHandListener implements Witness {

    // Author: Grinch 3/7/2018
    // Prevent item interaction with offhand torches
    @Listener
    public void onInteract(InteractItemEvent.Secondary.OffHand event, @Root Player player) {
        if (event.getItemStack().getType() == ItemTypes.REDSTONE_TORCH
                || event.getItemStack().getType() == ItemTypes.TORCH) {
            event.setCancelled(true);
        }
    }
}
