/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.cache.CacheFeature;
import com.almuradev.almura.feature.cache.tileentity.CacheTileEntity;
import com.almuradev.almura.shared.capability.ISingleSlotItemHandler;
import com.almuradev.almura.shared.capability.impl.SingleSlotItemHandler;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public final class CacheBlock extends BlockContainer {

    private final int slotLimit;

    // TODO Kashike, maybe move this to the loader
    public CacheBlock(ResourceLocation registryName, int slotLimit) {
        super(Material.GROUND);
        this.setRegistryName(registryName);
        this.setUnlocalizedName(registryName.getResourcePath().replace('/', '.'));
        this.setHardness(6.0F);
        this.setResistance(2000.0F);
        this.slotLimit = slotLimit;

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":storage").ifPresent((itemGroup) -> setCreativeTab((CreativeTabs) itemGroup));
    }

    public int getSlotLimit() {
        return this.slotLimit;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockHorizontal.FACING);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        final NBTTagCompound compound = stack.getSubCompound("tag");

        if (compound == null || !compound.hasKey("Cache")) {
            tooltip.add("Type: Empty");
            tooltip.add("Limit: " + CacheFeature.format.format(this.slotLimit));
            return;
        }

        final NBTTagCompound cacheCompound = compound.getCompoundTag("Cache");

        if (!cacheCompound.hasKey(Almura.ID + ":single_slot")) {
            tooltip.add("Type: Empty");
            tooltip.add("Limit: " + CacheFeature.format.format(this.slotLimit));
            return;
        }

        final NBTTagCompound slotCompound = cacheCompound.getCompoundTag(Almura.ID + ":single_slot");

        if (!slotCompound.hasKey("Slot")) {
            tooltip.add("Type: Empty");
        } else {
            final ItemStack cacheStack =
                    ((SingleSlotItemHandler.Storage) CacheFeature.SINGLE_SLOT_ITEM_HANDLER_CAPABILITY.getStorage()).readItemStackFromNBT(slotCompound
                            .getCompoundTag("Slot"));

            if (cacheStack.isEmpty()) {
                tooltip.add("Type: Empty");
            } else {
                tooltip.add("Type: " + cacheStack.getDisplayName());
                tooltip.add("Size: " + CacheFeature.format.format(cacheStack.getCount()));
            }
        }

        if (slotCompound.hasKey("SlotLimit")) {
            tooltip.add("Limit: " + CacheFeature.format.format(slotCompound.getInteger("SlotLimit")));
        } else {
            tooltip.add("Limit: " + CacheFeature.format.format(this.slotLimit));
        }
    }

    @Override
    protected boolean hasInvalidNeighbor(World worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        final CacheTileEntity cte = new CacheTileEntity();
        final ISingleSlotItemHandler itemHandler = (ISingleSlotItemHandler) cte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ((SingleSlotItemHandler) itemHandler).setSlotLimit(this.slotLimit);
        return cte;
    }

    // Left Click
    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
           return;
        }

        final TileEntity te = world.getTileEntity(pos);

        if (te == null || !(te instanceof CacheTileEntity)) {
            return;
        }

        final CacheTileEntity cte = (CacheTileEntity) te;

        final ISingleSlotItemHandler itemHandler = (ISingleSlotItemHandler) cte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (itemHandler == null) {
            return;
        }

        final ItemStack cacheStack = itemHandler.getStackInSlot(0);

        if (cacheStack.isEmpty()) {
            return;
        }

        int amountToExtract = 0;

        if (player.isSneaking()) {
            amountToExtract =1;
        } else {
            amountToExtract = Math.min(cacheStack.getItem().getItemStackLimit(), player.inventory.getInventoryStackLimit());
        }

        final ItemStack toExtract = itemHandler.extractItem(0, amountToExtract, true);

        if (toExtract.isEmpty()) {
            return;
        }

        final int preAddStackSize = toExtract.getCount();

        player.inventory.addItemStackToInventory(toExtract);

        final int postAddStackSize = toExtract.getCount();

        final int extractDiff = preAddStackSize - postAddStackSize;

        if (extractDiff <= 0) {
            ((Player) player).sendMessage(Text.of("Cannot withdrawal from cache as your inventory is full!"));
        } else {
            itemHandler.extractItem(0, extractDiff, false);

            ((Player) player).sendMessage(Text.of("Withdrew ", TextColors.AQUA, extractDiff, TextColors.WHITE, " ", cacheStack.getDisplayName(), " "
                    + "from the cache."));
        }
    }

    // Right click
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {

        if (world.isRemote) {
            return true;
        }

        if (player.isSneaking()) {
            return false;
        }

        final TileEntity te = world.getTileEntity(pos);
        if (te == null || !(te instanceof CacheTileEntity)) {
            return false;
        }

        final CacheTileEntity cte = (CacheTileEntity) te;

        final ISingleSlotItemHandler itemHandler = (ISingleSlotItemHandler) cte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (itemHandler == null) {
            return false;
        }

        boolean isDirty = false;
        /*
         * How cache interaction works:
         *
         * 1. If filled hand and cache does not exist, merge filled hand to become a new cache (up to cache limit).
         * 2. If empty hand and cache exists, withdrawal from the cache up to the inventory stack limit or the cache usage, whichever is lower.
         * 3. If cache exists, search for and merge appropriate stack from inventory (up to cache limit)
         */
        final ItemStack handStack = player.getHeldItem(hand);
        final ItemStack cacheStack = itemHandler.getStackInSlot(0);

        if (!handStack.isEmpty() && cacheStack.isEmpty()) {
            final ItemStack resultStack = itemHandler.insertItem(0, handStack, false);

            // Creative starts a new cache without removing items
            if (!player.isCreative()) {
                player.setHeldItem(hand, resultStack);
            }

            ((Player) player).sendMessage(Text.of("Deposited ", TextColors.AQUA, CacheFeature.format.format(Math.abs(resultStack.getCount() - handStack
                    .getCount())), TextColors.WHITE, " ", handStack.getDisplayName(), " into the cache."));

            isDirty = true;
        } else if (!cacheStack.isEmpty()) {
            if (!handStack.isEmpty() && !ItemStack.areItemsEqual(handStack, cacheStack)) {
                ((Player) player).sendMessage(Text.of("This cache does not support this item! Try emptying the cache first."));
                return true;
            }

            // Creative adds to cache indefinitely.
            if (player.isCreative() && !handStack.isEmpty()) {
                final ItemStack resultStack = itemHandler.insertItem(0, handStack, false);

                if (resultStack.getCount() == handStack.getCount()) {
                    ((Player) player).sendMessage(Text.of("Cannot add more to the cache as it is full!"));
                } else {

                    ((Player) player).sendMessage(Text.of("Deposited ", TextColors.AQUA, CacheFeature.format.format(handStack.getCount() -
                            resultStack
                            .getCount()), TextColors.WHITE, " ", handStack.getDisplayName(), " into the cache."));

                    isDirty = true;
                }
            } else {
                final boolean depositAll = handStack.isEmpty();

                // Search inventory for items that are similar to the cache (only non-matching aspect is the stack size)

                // TODO Please be kind PhaseTracker
                int deposited = 0;

                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    final ItemStack slotStack = player.inventory.getStackInSlot(i);
                    if (slotStack.isEmpty()) {
                        continue;
                    }

                    // Slot stack matches cache stack, merge it
                    if (slotStack.isItemEqual(cacheStack)) {
                        final ItemStack remaining = itemHandler.insertItem(0, slotStack, false);
                        player.inventory.setInventorySlotContents(i, remaining);

                        deposited += slotStack.getCount() - remaining.getCount();

                        isDirty = true;

                        if (!remaining.isEmpty()) {
                            break;
                        }

                        if (!depositAll) {
                            break;
                        }
                    }
                }

                if (deposited > 0) {
                    ((Player) player).sendMessage(Text.of("Deposited ", TextColors.AQUA, CacheFeature.format.format(deposited), TextColors.WHITE, " " ,
                            cacheStack.getDisplayName(), " into the cache."));
                }
            }
        }

        if (isDirty) {
            cte.markDirty();
        }

        return true;
    }

    private TileEntity cacheTe = null;

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        this.onBlockHarvested(world, pos, state, player);

        cacheTe = world.getTileEntity(pos);

        return world.setBlockState(pos, net.minecraft.init.Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (!(world instanceof WorldServer) || ((WorldServer) world).isRemote) {
            return;
        }

        final ItemStack toDrop = new ItemStack(this, 1, 0);

        drops.add(toDrop);

        TileEntity te = world.getTileEntity(pos);

        if (te == null) {
            te = cacheTe;
        }

        if (te == null || !(te instanceof CacheTileEntity)) {
            return;
        }

        final CacheTileEntity cte = (CacheTileEntity) te;


        this.addCacheToStack(toDrop, cte);

        cacheTe = null;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        final ItemStack pickStack = new ItemStack(this, 1, 0);

        final TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof CacheTileEntity) {
            final CacheTileEntity cte = (CacheTileEntity) te;

            // Alright, we have a Cache...need to put it on a stack now
            this.addCacheToStack(pickStack, cte);
        }

        return pickStack;
    }

    private ItemStack addCacheToStack(ItemStack targetStack, CacheTileEntity cte) {
        final ISingleSlotItemHandler itemHandler = (ISingleSlotItemHandler) cte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                null);

        if (itemHandler != null) {
            final ItemStack cacheStack = itemHandler.getStackInSlot(0);

            if (!cacheStack.isEmpty()) {

                final NBTTagCompound compound = new NBTTagCompound();
                final NBTTagCompound tagCompound = compound.getCompoundTag("tag");
                final NBTTagCompound cacheCompound = tagCompound.getCompoundTag("Cache");

                cacheCompound.setTag(Almura.ID + ":single_slot", CacheFeature.SINGLE_SLOT_ITEM_HANDLER_CAPABILITY.writeNBT(itemHandler, null));

                tagCompound.setTag("Cache", cacheCompound);
                compound.setTag("tag", tagCompound);
                targetStack.setTagCompound(compound);
            }
        }
        return targetStack;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (!worldIn.isRemote) {

            final NBTTagCompound tagCompound = stack.getSubCompound("tag");

            if (tagCompound == null) {
                return;
            }

            if (!tagCompound.hasKey("Cache")) {
                return;
            }

            final NBTTagCompound cacheCompound = tagCompound.getCompoundTag("Cache");

            final TileEntity te = worldIn.getTileEntity(pos);

            if (te == null || !(te instanceof CacheTileEntity)) {
                return;
            }

            final CacheTileEntity cte = (CacheTileEntity) te;
            final ISingleSlotItemHandler itemHandler = (ISingleSlotItemHandler) cte.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    null);

            if (itemHandler == null) {
                return;
            }

            CacheFeature.SINGLE_SLOT_ITEM_HANDLER_CAPABILITY.readNBT(itemHandler, null, cacheCompound.getCompoundTag(Almura.ID + ":single_slot"));
        }
    }
}
