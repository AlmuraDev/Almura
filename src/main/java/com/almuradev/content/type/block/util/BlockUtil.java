/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.util;

import static net.minecraft.block.Block.spawnAsEntity;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.component.apply.context.ApplyContext;
import com.almuradev.content.component.apply.context.EverythingApplyContext;
import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.type.action.component.drop.ItemDrop;
import com.almuradev.content.type.action.type.blockdecay.BlockDecayAction;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.mixin.iface.IMixinBlock;
import com.almuradev.content.type.block.mixin.iface.IMixinContentBlock;
import com.almuradev.content.type.block.mixin.iface.IMixinDecayBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.item.ItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public class BlockUtil {

    public static void handleHarvest(final IMixinContentBlock block, final World world, final EntityPlayer player, final BlockPos pos, final
    IBlockState state, @Nullable final TileEntity te, final ItemStack stack) {
        // Almura Start - If this is the client or Player is creative, no harvest
        if (world.isRemote || player.isCreative()) {
            return;
        }

        // Now check if we have no breaks, this block is not meant to perform any logic on harvest
        final BlockDestroyAction blockDestroyAction = block.destroyAction(state);
        if (blockDestroyAction == null || blockDestroyAction.entries().isEmpty()) {
            return;
        }

        final Block mcBlock = (Block) (Object) block;

        player.addStat(StatList.getBlockStats(mcBlock));

        if (canSilkHarvest(state) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();
            ItemStack itemstack = getSilkTouchDrop(state);

            if (!itemstack.isEmpty()) {
                items.add(itemstack);
            }

            net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1.0f, true, player);
            for (ItemStack item : items) {
                spawnAsEntity(world, pos, item);
            }
        } else {

            // Almura End

            block.getHarvesters().set(player);

            // Almura Start - Run through the kitkats and break!
            if (!fireBreakActions((ItemType) stack.getItem(), state, player, pos, world.rand, stack, blockDestroyAction)) {
                // Fallback to empty action block if nothing overrides it
                fireBreakActions(org.spongepowered.api.item.inventory.ItemStack.empty().getType(), state, player, pos, world.rand, stack,
                        blockDestroyAction);
            }

            // TODO Expose fortune to config and see if admin wants to let it use it
            final int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            if (!fireHarvestAndDrop(block, (ItemType) stack.getItem(), world, pos, state, 1f, fortune, blockDestroyAction)) {
                // Fallback to empty drop block if nothing overrides it
                fireHarvestAndDrop(block, org.spongepowered.api.item.inventory.ItemStack.empty().getType(), world, pos, state, 1f, fortune,
                        blockDestroyAction);
            }

            // Almura End

            block.getHarvesters().set(null);
        }
    }

    // Almura Start -> Handle lookup of Silk Properties
    // ToDo: Make this configurable per Block

    private static boolean canSilkHarvest(IBlockState state) {
        return state.getBlock().getDefaultState().isFullCube() && !state.getBlock().hasTileEntity(state);
    }

    private static ItemStack getSilkTouchDrop(IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        int i = 0;

        if (item.getHasSubtypes()) {
            i = state.getBlock().getMetaFromState(state);
        }

        return new ItemStack(item, 1, i);
    }
    // Almura End

    public static void handleDropBlockAsItemWithChance(final IMixinContentBlock block, final World world, final BlockPos pos, final IBlockState
            state, float chance, final int fortune) {
        if (!world.isRemote && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
            // Almura Start - We don't use the drops here
            // List<ItemStack> drops = getDrops(world, pos, state, fortune); // use the old method until it gets removed, for backward compatibility
            // Almura End

            final IMixinBlock coreBlock = (IMixinBlock) block;

            if (!coreBlock.dropsFromDecay()) {
                // Now check if we have no breaks, this block is not meant to perform any logic on harvest
                final BlockDestroyAction blockDestroyAction = block.destroyAction(state);
                if (blockDestroyAction == null || blockDestroyAction.entries().isEmpty()) {
                    return;
                }

                fireHarvestAndDrop(block, org.spongepowered.api.item.inventory.ItemStack.empty().getType(), world, pos, state, chance, fortune,
                        blockDestroyAction);
            } else {
                final IMixinDecayBlock decayBlock = (IMixinDecayBlock) block;
                final BlockDecayAction blockDecayAction = decayBlock.decayAction(state);
                if (blockDecayAction == null || blockDecayAction.entries().isEmpty()) {
                    return;
                }

                final Biome biome = world.getBiome(pos);
                final List<ItemStack> drops = new ArrayList<>();

                for (final BlockDecayAction.Entry entry : blockDecayAction.entries()) {
                    if (entry.test(biome, world.rand)) {
                        for (final Drop drop : entry.drops()) {
                            if (drop instanceof ItemDrop) {
                                ((ItemDrop) drop).fill(drops, world.rand);
                            }
                        }
                    }
                }

                if (drops.isEmpty()) {
                    return;
                }

                chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, chance, false,
                        block.getHarvesters().get());

                if (chance == 0) {
                    return;
                }

                drop(world, pos, chance, drops);
            }
        }
    }

    private static boolean fireBreakActions(final ItemType usedType, final IBlockState state, final EntityPlayer player, final BlockPos pos, final
    Random random, final ItemStack stack, @Nullable final BlockDestroyAction destroyAction) {
        if (destroyAction == null) {
            return true;
        }

        boolean hasActions = false;

        final ApplyContext context = new EverythingApplyContext(random, pos, state, stack);
        for (final BlockDestroyAction.Entry entry : destroyAction.entries()) {
            if (entry.test(usedType)) {
                for (final Apply action : entry.apply()) {
                    if (action.accepts(player)) {
                        hasActions = true;
                        action.apply(player, context);
                    }
                }
            }
        }

        return hasActions;
    }

    private static boolean fireHarvestAndDrop(final IMixinContentBlock block, final ItemType type, final World world, final BlockPos pos,
            final IBlockState state, float chance, final int fortune, @Nullable final BlockDestroyAction destroyAction) {

        if (destroyAction == null) {
            return true;
        }

        final List<ItemStack> drops = new ArrayList<>();

        for (final BlockDestroyAction.Entry entry : destroyAction.entries()) {
            if (entry.test(type)) {
                for (final Drop drop : entry.drops()) {
                    if (drop instanceof ItemDrop) {
                        ((ItemDrop) drop).fill(drops, world.rand);
                    }
                }
            }
        }

        if (drops.isEmpty()) {
            return false;
        }

        chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, chance, false,
                block.getHarvesters().get());

        if (chance == 0) {
            return false;
        }

        drop(world, pos, chance, drops);

        return true;
    }

    private static void drop(final World world, final BlockPos pos, final float chance, final List<ItemStack> drops) {
        for (final ItemStack drop : drops) {
            if (world.rand.nextFloat() <= chance) {
                spawnAsEntity(world, pos, drop);
            }
        }
    }
}
