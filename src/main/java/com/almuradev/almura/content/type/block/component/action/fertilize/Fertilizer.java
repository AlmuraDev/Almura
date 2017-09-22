/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.fertilize;

import com.almuradev.almura.content.type.block.type.crop.FertilizableBlock;
import com.almuradev.shared.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Fertilizer implements Witness {

    @SubscribeEvent
    public void bonemeal(final BonemealEvent event) {
        final IBlockState state = event.getBlock();
        final Block block = state.getBlock();
        if (!(block instanceof FertilizableBlock)) {
            return;
        }

        // TODO
    }
}
