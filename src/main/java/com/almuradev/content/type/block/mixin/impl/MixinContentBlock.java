/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.mixin.impl;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.component.apply.context.ApplyContext;
import com.almuradev.content.component.apply.context.EverythingApplyContext;
import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.type.action.component.drop.ItemDrop;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.SpecialBlockStateBlock;
import com.almuradev.content.type.block.mixin.iface.IMixinContentBlock;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.block.type.horizontal.HorizontalBlockImpl;
import com.almuradev.content.type.block.type.normal.NormalBlockImpl;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroup;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.mixin.iface.IMixinLazyItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin({
    HorizontalBlockImpl.class,
    NormalBlockImpl.class,
    CropBlockImpl.class
})
public abstract class MixinContentBlock extends MixinBlock implements ContentBlockType, IMixinContentBlock, IMixinLazyItemGroup, SpecialBlockStateBlock {

    @Nullable private Delegate<ItemGroup> lazyItemGroup;
    private ResourceLocation blockStateDefinitionLocation;

    @Override
    public ResourceLocation blockStateDefinitionLocation() {
        return this.blockStateDefinitionLocation;
    }

    @Override
    public void blockStateDefinitionLocation(final ResourceLocation location) {
        this.blockStateDefinitionLocation = location;
    }

    @Override
    public Optional<ItemGroup> itemGroup() {
        if (this.displayOnCreativeTab != null) {
            return Optional.of((ItemGroup) this.displayOnCreativeTab);
        }
        if (this.lazyItemGroup == null) {
            return Optional.empty();
        }
        this.displayOnCreativeTab = (CreativeTabs) this.lazyItemGroup.get();
        return Optional.ofNullable((ItemGroup) this.displayOnCreativeTab);
    }

    @Override
    public void itemGroup(final Delegate<ItemGroup> group) {
        this.lazyItemGroup = group;
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return (CreativeTabs) this.itemGroup().orElse(null);
    }

    @Override
    public SoundType getSoundType(final IBlockState state, final World world, final BlockPos pos, @Nullable final Entity entity) {
        return (SoundType) this.soundGroup(state).orElse((BlockSoundGroup) super.getSoundType(state, world, pos, entity));
    }

    @Override
    public Optional<BlockSoundGroup> soundGroup(final IBlockState state) {
        return Delegate.optional(((BlockStateDefinition.Impl<?, ?, ?>) this.definition(state)).sound);
    }

    @Nullable
    @Override
    public BlockDestroyAction destroyAction(final IBlockState state) {
        return Delegate.get(((BlockStateDefinition.Impl<?, ?, ?>) this.definition(state)).destroyAction);
    }

    // Almura Start - Handle drops from Break
    @Override
    public void harvestBlock(final World world, final EntityPlayer player, final BlockPos pos, final IBlockState state, @Nullable final TileEntity te, final ItemStack stack) {
        // Almura Start - If this is the client
        if (world.isRemote) {
            return;
        }

        // Now check if we have no breaks, this block is not meant to perform any logic on harvest
        final BlockDestroyAction blockDestroyAction = this.destroyAction(state);
        if (blockDestroyAction == null || blockDestroyAction.entries().isEmpty()) {
            return;
        }

        player.addStat(StatList.getBlockStats((Block) (Object) this));

        // Almura Start - For now, our custom blocks don't do silk harvest
/*
            if (this.canSilkHarvest(world, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
            {
                java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();
                ItemStack itemstack = this.getSilkTouchDrop(state);

                if (!itemstack.isEmpty())
                {
                    items.add(itemstack);
                }

                net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1.0f, true, player);
                for (ItemStack item : items)
                {
                    spawnAsEntity(world, pos, item);
                }
            }
            else
            {
 */
        // Almura End

        harvesters.set(player);

        // Almura Start - Run through the kitkats and break!
        if (!this.fireBreakActions((ItemType) stack.getItem(), state, player, pos, world.rand, stack, blockDestroyAction)) {
            // Fallback to empty action block if nothing overrides it
            this.fireBreakActions(org.spongepowered.api.item.inventory.ItemStack.empty().getItem(), state, player, pos, world.rand, stack, blockDestroyAction);
        }

        // TODO Expose fortune to config and see if admin wants to let it use it
        final int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
        if (!this.fireHarvestAndDrop((ItemType) stack.getItem(), world, pos, state, 1f, fortune, blockDestroyAction)) {
            // Fallback to empty drop block if nothing overrides it
            this.fireHarvestAndDrop(org.spongepowered.api.item.inventory.ItemStack.empty().getItem(), world, pos, state, 1f, fortune, blockDestroyAction);
        }

        // Almura End

        harvesters.set(null);
        //}
    }

    @Override
    public void dropBlockAsItemWithChance(final World world, final BlockPos pos, final IBlockState state, final float chance, final int fortune) {
        if (!world.isRemote && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
            // Almura Start - We don't use the drops here
            // List<ItemStack> drops = getDrops(world, pos, state, fortune); // use the old method until it gets removed, for backward compatibility
            // Almura End

            // Now check if we have no breaks, this block is not meant to perform any logic on harvest
            final BlockDestroyAction blockDestroyAction = this.destroyAction(state);
            if (blockDestroyAction == null || blockDestroyAction.entries().isEmpty()) {
                return;
            }

            this.fireHarvestAndDrop(org.spongepowered.api.item.inventory.ItemStack.empty().getItem(), world, pos, state, chance, fortune, blockDestroyAction);
        }
    }

    private boolean fireBreakActions(final ItemType usedType, final IBlockState state, final EntityPlayer player, final BlockPos pos, final Random
            random, final ItemStack stack, @Nullable final BlockDestroyAction destroyAction) {
        if (destroyAction == null) {
            return false;
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

    private boolean fireHarvestAndDrop(final ItemType type, final World world, final BlockPos pos, final IBlockState state, float chance, final int
            fortune, @Nullable final BlockDestroyAction destroyAction) {

        if (destroyAction == null) {
            return false;
        }

        final List<ItemStack> drops = new ArrayList<>();

        for (final BlockDestroyAction.Entry entry : destroyAction.entries()) {
            if (entry.test(type)) {
                for (final Drop drop : entry.drops()) {
                    if (drop instanceof ItemDrop) {
                        ((ItemDrop) drop).fill(drops);
                    }
                }
            }
        }

        if (drops.isEmpty()) {
            return false;
        }

        chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, chance, false, this.harvesters.get());

        for (final ItemStack drop : drops) {
            if (world.rand.nextFloat() <= chance) {
                Block.spawnAsEntity(world, pos, drop);
            }
        }

        return true;
    }
    // Almura End - Handle drops from Break
}
