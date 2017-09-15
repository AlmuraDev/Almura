/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block;

import com.almuradev.almura.asm.mixin.interfaces.IMixinAlmuraBlock;
import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateBlockAttributes;
import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateMaterialAttributes;
import com.almuradev.almura.content.type.block.component.action.Action;
import com.almuradev.almura.content.type.block.component.action.breaks.BlockBreak;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.Drop;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.ItemDrop;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.block.type.generic.GenericBlock;
import com.almuradev.almura.content.type.block.type.horizontal.GenericHorizontalBlock;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin(value = {GenericBlock.class, GenericHorizontalBlock.class})
public abstract class MixinAlmuraBlock extends MixinBlock implements BuildableBlockType, IMixinDelegateMaterialAttributes,
        IMixinDelegateBlockAttributes, IMixinAlmuraBlock {

    @Nullable private CatalogDelegate<ItemGroup> itemGroupDelegate;
    @Nullable private CatalogDelegate<BlockSoundGroup> soundGroupDelegate;
    private List<BlockBreak> breaks;

    @Override
    public Optional<ItemGroup> getItemGroup() {
        if (this.displayOnCreativeTab != null) {
            return Optional.of((ItemGroup) this.displayOnCreativeTab);
        }

        // Having no delegate instance means it was truly null
        if (this.itemGroupDelegate == null) {
            return Optional.empty();
        }

        final ItemGroup cached = this.itemGroupDelegate.get();
        this.displayOnCreativeTab = (CreativeTabs) cached;

        return Optional.of((ItemGroup) this.displayOnCreativeTab);
    }

    @Override
    public void setItemGroupDelegate(CatalogDelegate<ItemGroup> itemGroupDelegate) {
        this.itemGroupDelegate = itemGroupDelegate;
    }

    @SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTabToDisplayOn() {
        return (CreativeTabs) this.getItemGroup().orElse(null);
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return this.soundGroupDelegate != null ? (SoundType) this.soundGroupDelegate.get() : super.getSoundType(state, world, pos, entity);
    }

    @Override
    public void setSoundGroupDelegate(CatalogDelegate<BlockSoundGroup> blockSoundGroupDelegate) {
        this.soundGroupDelegate = blockSoundGroupDelegate;
    }

    @Override
    public List<BlockBreak> getBreaks() {
        return this.breaks;
    }

    @Override
    public void setBreaks(List<BlockBreak> breaks) {
        this.breaks = breaks;
    }

    // Almura Start - Handle drops from Break
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        // Almura Start - If this is the client or if we have no breaks, this block is not meant to perform any drops
        if (worldIn.isRemote || this.breaks.isEmpty()) {
            return;
        }

        player.addStat(StatList.getBlockStats((Block) (Object) this));

        // Almura Start - For now, our custom blocks don't do silk harvest
/*
            if (this.canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
            {
                java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();
                ItemStack itemstack = this.getSilkTouchDrop(state);

                if (!itemstack.isEmpty())
                {
                    items.add(itemstack);
                }

                net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true, player);
                for (ItemStack item : items)
                {
                    spawnAsEntity(worldIn, pos, item);
                }
            }
            else
            {
 */
        // Almura End

        harvesters.set(player);

        // Almura Start - Run through the kitkats and break!
        if (!this.fireBreakActions((ItemType) stack.getItem(), player, pos, worldIn.rand, stack)) {
            // Fallback to empty action block if nothing overrides it
            this.fireBreakActions(org.spongepowered.api.item.inventory.ItemStack.empty().getItem(), player, pos, worldIn.rand, stack);
        }

        // TODO Expose fortune to config and see if admin wants to let it use it
        final int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
        if (!this.fireHarvestAndDrop((ItemType) stack.getItem(), worldIn, pos, state, 1f, fortune)) {
            // Fallback to empty drop block if nothing overrides it
            this.fireHarvestAndDrop(org.spongepowered.api.item.inventory.ItemStack.empty().getItem(), worldIn, pos, state, 1f, fortune);
        }

        // Almura End

        harvesters.set(null);
        //}
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
            // Almura Start - We don't use the drops here
            // List<ItemStack> drops = getDrops(worldIn, pos, state, fortune); // use the old method until it gets removed, for backward compatibility
            // Almura End
            this.fireHarvestAndDrop(org.spongepowered.api.item.inventory.ItemStack.empty().getItem(), worldIn, pos, state, chance, fortune);
        }
    }

    private boolean fireBreakActions(ItemType usedType, EntityPlayer player, BlockPos pos, Random random, ItemStack stack) {
        boolean hasActions = false;

        for (final BlockBreak kitkat : this.breaks) {
            if (kitkat.accepts(usedType)) {
                for (Action action : kitkat.getActions()) {
                    hasActions = true;
                    action.apply(player, (Block) (Object) this, pos, random, stack);
                }
            }
        }

        return hasActions;
    }

    private boolean fireHarvestAndDrop(ItemType usedType, World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        final List<ItemStack> drops = new ArrayList<>();

        for (final BlockBreak kitkat : this.breaks) {
            if (kitkat.accepts(usedType)) {
                for (Drop drop : kitkat.getDrops()) {
                    if (drop instanceof ItemDrop) {
                        for (ItemStackSnapshot itemStackSnapshot : ((ItemDrop) drop).getDrops()) {
                            final org.spongepowered.api.item.inventory.ItemStack stack = itemStackSnapshot.createStack();
                            stack.setQuantity(drop.flooredAmount(world.rand));
                            drops.add((ItemStack) (Object) stack);
                        }
                    }
                }
            }
        }

        if (drops.isEmpty()) {
            return false;
        }

        chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, chance, false, this.harvesters.get());

        for (ItemStack drop : drops) {
            if (world.rand.nextFloat() <= chance) {
                Block.spawnAsEntity(world, pos, drop);
            }
        }

        return true;
    }
    // Almura End - Handle drops from Break
}
