/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.wand;

import com.almuradev.almura.shared.event.Witness;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumSkyBlock;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;

public final class WandFeature implements Witness {

    @Listener
    public void interact(final InteractBlockEvent.Secondary.MainHand event, @Root final Player player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        // Known:  this event only seems to fire when on the server.

        //TODO:  Can't this be in some sort of Item right-click event rather than listening to ever interact?

        final ItemStack item = ((EntityPlayerMP) player).getHeldItemMainhand();
        System.out.println("Item: " + item.getUnlocalizedName());
        // Light Repair Wand
        if (item.getUnlocalizedName().equalsIgnoreCase("item.almura.normal.tool.light_repair_wand")) {
            System.out.println("I'm getting here twice");

            final BlockPos pos = ((EntityPlayerMP) player).getPosition();
            final net.minecraft.world.World world = (net.minecraft.world.World) player.getWorld();
            for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-100, -20, -100), pos.add(100, 20, 100))) {
                world.setLightFor(EnumSkyBlock.BLOCK, blockpos$mutableblockpos, 0);
                if (world.getBlockState(blockpos$mutableblockpos).getBlock() == Blocks.TORCH) {
                    world.setBlockToAir(blockpos$mutableblockpos);
                }

                if (world.getBlockState(blockpos$mutableblockpos).getBlock() == Blocks.GLOWSTONE) {
                    world.setBlockToAir(blockpos$mutableblockpos);
                }
            }

            ((EntityPlayerMP) player).sendMessage(new TextComponentString("Light values within a -100/[40]+100 range have been fixed.  Unload and reload chunks to get client ot update."));
        }

    }
}
