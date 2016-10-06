/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public final class CachesBlock extends BlockContainer implements IPackObject {

    private final int initialStackSize;

    public CachesBlock(String unlocalizedName, String displayName, int initialStackSize, CreativeTabs tabs) {
        super(Material.rock);
        this.initialStackSize = initialStackSize;
        setUnlocalizedName(unlocalizedName);
        setTextureName(Almura.MOD_ID + ":textures/" + unlocalizedName);
        setHardness(50.0F);
        setResistance(2000.0F);
        setStepSound(soundTypePiston);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, this.getUnlocalizedName() + ".name", displayName);
        if (tabs != null) {
            setCreativeTab(tabs);
        }
    }

    @Override
    protected void dropBlockAsItem(World worldIn, int x, int y, int z, ItemStack itemIn) {
        if (!worldIn.isRemote && worldIn.getGameRules().getGameRuleBooleanValue("doTileDrops")
                && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe.
            if (captureDrops.get()) {
                capturedDrops.get().add(itemIn);
                return;
            }

            final TileEntity te = worldIn.getTileEntity(x, y, z);

            // TODO Once cache limit is set on all stacks, move this out of this check
            if (te instanceof CachesTileEntity && ((CachesTileEntity) te).getCache() != null) {

                final NBTTagCompound cachesCompound = new NBTTagCompound();
                cachesCompound.setInteger(CachesTileEntity.TAG_CACHE_MAX_STACK_SIZE, ((CachesTileEntity) te).getInventoryStackLimit());

                final NBTTagCompound cachesContentsCompound = new NBTTagCompound();
                cachesCompound.setTag(CachesTileEntity.TAG_CACHE_CONTENTS, ((CachesTileEntity) te).getCache().writeToNBT(cachesContentsCompound));

                final NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setTag(CachesTileEntity.TAG_CACHE, cachesCompound);

                final NBTTagCompound compound = new NBTTagCompound();
                compound.setTag("tag", tagCompound); //Straight from ItemStack

                itemIn.setTagCompound(compound);

                itemIn.setStackDisplayName(itemIn.getDisplayName() + " (" + ((CachesTileEntity) te).getCache().getDisplayName() + ")");
            }

            float f = 0.7F;
            double d0 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            double d1 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            double d2 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            EntityItem entityitem = new EntityItem(worldIn, (double) x + d0, (double) y + d1, (double) z + d2, itemIn);
            entityitem.delayBeforeCanPickup = 10;
            worldIn.spawnEntityInWorld(entityitem);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (!worldIn.isRemote) {
            final TileEntity te = worldIn.getTileEntity(x, y, z);
            if (!(te instanceof CachesTileEntity)) {
                return false;
            }

            final ItemStack cache = ((CachesTileEntity) te).getCache();
            final ItemStack held = player.getHeldItem();

            // TODO Fix dupe code below...
            // If you right-click with empty hand, withdrawal stacks of whatever the Player inventory maximum stack size is or whatever the
            // cache has left, whichever is lower.
            if (held == null && cache != null) {
                final int cacheSize = cache.stackSize;

                ItemStack toAdd = new ItemStack(cache.getItem(), player.inventory.getInventoryStackLimit() >
                        cacheSize ? player.inventory.getInventoryStackLimit() : cacheSize, cache.getMetadata());

                if (!player.inventory.addItemStackToInventory(toAdd)) {
                    player.addChatComponentMessage(new ChatComponentText("Cannot withdrawal from cache as your inventory is full!"));
                    return false;
                } else {
                    if (toAdd.stackSize == 0) {
                        toAdd = null;
                    }

                    ((CachesTileEntity) te).setInventorySlotContents(0, toAdd);
                }

                // If you right click with filled hand and empty cache, merge it.
            } else if (held != null && cache == null) {
                final int heldStackSize = held.stackSize;
                player.setCurrentItemOrArmor(0, ((CachesTileEntity) te).mergeStackIntoSlot(held));
                player.addChatComponentMessage(new ChatComponentText("Added " + (player.getHeldItem() == null ? heldStackSize : player
                        .getHeldItem().stackSize - heldStackSize) + " to the cache."));

                // If you right click with filled hand and a cache who is not empty, merge a stack from our inventory into the cache.
            } else if (held != null) {
                // Search inventory for items that are similar to the cache (only non-matching aspect is the stack size)
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    final ItemStack slotStack = player.inventory.getStackInSlot(i);
                    if (slotStack == null) {
                        continue;
                    }

                    // Slot stack matches cache stack, merge it
                    if (slotStack.isItemEqual(cache)) {
                        final int slotStackSize = slotStack.stackSize;
                        final ItemStack remaining = ((CachesTileEntity) te).mergeStackIntoSlot(slotStack);
                        if (remaining == null) {
                            player.inventory.setInventorySlotContents(i, null);
                            player.addChatComponentMessage(new ChatComponentText("Added " + slotStackSize + " to the cache."));
                        } else {
                            player.addChatComponentMessage(new ChatComponentText("Added " + (slotStackSize - remaining.stackSize) + " to the "
                                    + "cache."));
                        }
                        break;
                    }
                }

            }

            player.openContainer.detectAndSendChanges();
        }
        return true;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, int x, int y, int z, int meta) {
        super.harvestBlock(worldIn, player, x, y, z, meta);
        worldIn.setBlockToAir(x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        if (!worldIn.isRemote) {
            final TileEntity te = worldIn.getTileEntity(x, y, z);
            if (!(te instanceof CachesTileEntity)) {
                return;
            }

            if (itemIn.hasTagCompound()) {
                final NBTTagCompound compound = itemIn.getTagCompound();

                if (compound.hasKey("tag")) {
                    final NBTTagCompound tagCompound = compound.getCompoundTag("tag");

                    if (tagCompound.hasKey(CachesTileEntity.TAG_CACHE)) {
                        final NBTTagCompound cachesCompound = tagCompound.getCompoundTag(CachesTileEntity.TAG_CACHE);

                        final int maxStackSize;
                        if (cachesCompound.hasKey(CachesTileEntity.TAG_CACHE_MAX_STACK_SIZE)) {
                            maxStackSize = cachesCompound.getInteger(CachesTileEntity.TAG_CACHE_MAX_STACK_SIZE);
                        } else {
                            maxStackSize = CachesTileEntity.DEFAULT_MAX_STACK_SIZE;
                        }

                        final ItemStack cache;
                        if (cachesCompound.hasKey(CachesTileEntity.TAG_CACHE_CONTENTS)) {
                            cache = ItemStack.loadItemStackFromNBT(cachesCompound.getCompoundTag(CachesTileEntity.TAG_CACHE_CONTENTS));
                        } else {
                            cache = null;
                        }

                        ((CachesTileEntity) te).setInventoryStackLimit(maxStackSize);
                        if (cache != null) {
                            ((CachesTileEntity) te).mergeStackIntoSlot(cache);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(world, player, x, y, z, false);
    }

    @Override
    public boolean isToolEffective(String type, int metadata) {
        return super.isToolEffective(type, metadata);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new CachesTileEntity(this.initialStackSize);
    }

    @Override
    public Pack getPack() {
        return Almura.INTERNAL_PACK;
    }

    @Override
    public String getIdentifier() {
        return this.getUnlocalizedName();
    }

    public int getCacheLimit() {
        return this.initialStackSize;
    }
}
