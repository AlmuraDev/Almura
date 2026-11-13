/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.offhand;

import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.minecraft.block.BlockTorch;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.inject.Inject;

public final class OffHandListener implements Witness {

    private final MappedConfiguration<ClientConfiguration> configAdapter;

    @Inject
    public OffHandListener(final MappedConfiguration<ClientConfiguration> configAdapter) {
        this.configAdapter = configAdapter;
    }

    // Author: Grinch 3/21/2018
    // Prevents interaction with torches from offhand if configuration option is enabled
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!this.configAdapter.get().general.disableOffhandTorchPlacement) {
            return;
        }

        if (event.getHand() == EnumHand.OFF_HAND) {
            final Item item = event.getItemStack().getItem();
            if (item instanceof ItemBlock) {
                if (((ItemBlock) item).getBlock() instanceof BlockTorch) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
