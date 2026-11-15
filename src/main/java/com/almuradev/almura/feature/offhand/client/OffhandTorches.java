/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.offhand.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

/**
 * Disable Offhand Torch Placement (Options), as 3.5's {@code OffHandListener}: with it on, a torch held in the off hand isn't used, so a
 * right click with the main hand empty doesn't place it. Stopped on the client before anything is sent, so the server never places it either.
 */
public final class OffhandTorches {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInteraction(final InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || event.getHand() != InteractionHand.OFF_HAND
            || !AlmuraClientConfig.get(AlmuraClientConfig.DISABLE_OFFHAND_TORCH_PLACEMENT)) {
            return;
        }
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getOffhandItem().getItem() instanceof final BlockItem item && item.getBlock() instanceof BaseTorchBlock) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }
}
