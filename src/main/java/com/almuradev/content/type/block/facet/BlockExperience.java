/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.facet;

import com.almuradev.almura.shared.event.Witness;
import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.type.action.component.drop.ExperienceDrop;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.mixin.iface.IMixinAlmuraBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Random;

public final class BlockExperience implements Witness {

    private static final int NO_EXPERIENCE = -1;

    // This is only necessary to get xp handled correctly for Player breaks in the Forge ecosystem
    // Make sure to attempt to go first so we can set xp so that mods/plugins can override us if necessary
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void destroy(final BlockEvent.BreakEvent event) {
        final World world = event.getWorld();
        final IBlockState state = world.getBlockState(event.getPos());
        final Block block = state.getBlock();
        if (!(block instanceof IMixinAlmuraBlock) || ((IMixinAlmuraBlock) block).destroyAction(state) == null) {
            return;
        }

        int experience = this.calculate((ItemType) event.getPlayer().getActiveItemStack().getItem(), state, (IMixinAlmuraBlock) block, world.rand);

        if (experience == NO_EXPERIENCE) {
            // if no exp for this itemstack, fallback to empty hand and check again
            experience = this.calculate(ItemStack.empty().getType(), state, (IMixinAlmuraBlock) block, world.rand);
        }

        if (experience != NO_EXPERIENCE) {
            event.setExpToDrop(experience);
        }
    }

    private int calculate(final ItemType with, final IBlockState state, final IMixinAlmuraBlock block, final Random random) {
        int experience = NO_EXPERIENCE;
        for (final BlockDestroyAction.Entry entry : block.destroyAction(state).entries()) {
            if (entry.test(with)) {
                for (final Drop drop : entry.drops()) {
                    if (drop instanceof ExperienceDrop) {
                        experience += drop.flooredAmount(random);
                    }
                }
            }
        }
        return experience;
    }
}
