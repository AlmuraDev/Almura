/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block;

import com.almuradev.almura.asm.mixin.interfaces.IMixinAlmuraBlock;
import com.almuradev.almura.content.type.block.component.action.breaks.BlockBreak;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.Drop;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.ExperienceDrop;
import com.almuradev.shared.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Random;

public class BlockListener implements Witness {

    private static final int NO_EXPERIENCE = -1;

    // This is only necessary to get xp handled correctly for Player breaks in the Forge ecosystem
    // Make sure to attempt to go first so we can set xp so that mods/plugins can override us if necessary
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        final Block block = event.getWorld().getBlockState(event.getPos()).getBlock();

        if (!(block instanceof IMixinAlmuraBlock)) {
            return;
        }

        final World world = event.getWorld();
        int exp = this.getXpToDrop((ItemType) event.getPlayer().getActiveItemStack().getItem(), (IMixinAlmuraBlock) block, world.rand);

        if (exp == NO_EXPERIENCE) {
            // if no exp for this itemstack, fallback to empty hand and check again
            exp = this.getXpToDrop(ItemStack.empty().getType(), (IMixinAlmuraBlock) block, world.rand);
        }

        if (exp != NO_EXPERIENCE) {
            event.setExpToDrop(exp);
        }
    }

    private int getXpToDrop(ItemType with, IMixinAlmuraBlock block, Random random) {
        int exp = NO_EXPERIENCE;

        for (BlockBreak kitkat : block.getBreaks()) {
            if (kitkat.accepts(with)) {
                for (Drop drop : kitkat.getDrops()) {
                    if (drop instanceof ExperienceDrop) {
                        exp += drop.flooredAmount(random);
                    }
                }
            }
        }

        return exp;
    }
}
