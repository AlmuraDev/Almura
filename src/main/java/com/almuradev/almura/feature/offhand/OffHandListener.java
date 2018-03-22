/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.offhand;

import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.core.event.Witness;
import net.minecraft.block.BlockTorch;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OffHandListener implements Witness {

    // Author: Grinch 3/21/2018
    // Prevents interaction with torches from offhand if configuration option is enabled
    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!StaticAccess.config.get().client.disableOffhandTorchPlacement) {
            return;
        }

        if (event.getHand() == EnumHand.OFF_HAND) {
            final Item item = event.getItemStack().getItem();
            if (item instanceof ItemBlock) {
                if (((ItemBlock) item).getBlock() instanceof BlockTorch) {
                    event.setUseItem(Event.Result.DENY);
                }
            }
        }
    }
}
