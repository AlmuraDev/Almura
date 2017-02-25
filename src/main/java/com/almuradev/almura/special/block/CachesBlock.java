/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.util.ExternalIcon;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.container.PackContainerTileEntity;
import com.almuradev.almura.util.FileSystem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public final class CachesBlock extends BlockContainer implements IPackObject {

    private final int cacheLimit;
    private IIcon tb, side, front;
    private final String displayName;

    CachesBlock(String unlocalizedName, String textureName, String displayName, int cacheLimit, CreativeTabs tabs) {
        super(Material.rock);
        this.cacheLimit = cacheLimit;
        setUnlocalizedName(unlocalizedName);
        setTextureName(Almura.MOD_ID + ":internal/blocks/" + textureName);
        setHardness(20.0F);
        setResistance(2000.0F);
        setStepSound(soundTypePiston);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, this.getUnlocalizedName() + ".name", displayName);
        this.displayName = displayName;
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

            if (te instanceof CachesTileEntity && ((CachesTileEntity) te).getCache() != null) {

                final NBTTagCompound cachesCompound = new NBTTagCompound();
                cachesCompound.setInteger(CachesTileEntity.TAG_CACHE_MAX_STACK_SIZE, ((CachesTileEntity) te).getInventoryStackLimit());

                final NBTTagCompound cachesContentsCompound = new NBTTagCompound();
                cachesCompound.setTag(CachesTileEntity.TAG_CACHE_CONTENTS, CachesTileEntity.writeToNBT(((CachesTileEntity) te)
                        .getCache(), cachesContentsCompound));

                final NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setTag(CachesTileEntity.TAG_CACHE, cachesCompound);

                final NBTTagCompound compound = new NBTTagCompound();
                compound.setTag("tag", tagCompound); //Straight from ItemStack

                itemIn.setTagCompound(compound);

                itemIn.setStackDisplayName(((CachesBlock) ((CachesItemBlock) itemIn.getItem()).blockInstance).getDisplayName() + " (" + (
                        (CachesTileEntity) te).getCache().getDisplayName() + ")");
            }

            final float f = 0.7F;
            final double d0 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            final double d1 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            final double d2 = (double) (worldIn.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
            final EntityItem entityitem = new EntityItem(worldIn, (double) x + d0, (double) y + d1, (double) z + d2, itemIn);
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
            boolean depositAll = false;
            final ItemStack cache = ((CachesTileEntity) te).getCache();
            final ItemStack held = player.getHeldItem();

            /*
             * How cache interaction works:
             *
             * 1. If empty hand and cache exists, withdrawal from the cache up to the inventory stack limit or the cache usage, whichever is lower.
             * 2. If filled hand and cache does not exist, merge filled hand to become a new cache (up to cache limit).
             * 3. If cache exists, search for and merge appropriate stack from inventory (up to cache limit)
             */

            if (player.isSneaking()) {
                return false;
            }
            
            if (player.getHeldItem() == null) {
                depositAll = true;
            }
            
            if (held != null && cache == null) {
                final int heldStackSize = held.stackSize;
                player.setCurrentItemOrArmor(0, ((CachesTileEntity) te).mergeStackIntoSlot(held));
                player.addChatComponentMessage(new ChatComponentText("Added " + (player.getHeldItem() == null ? heldStackSize : player
                        .getHeldItem().stackSize - heldStackSize) + " to the cache."));
            } else if (cache != null) {
                // Creative allows you to just infinitely fill the cache
                if (player.capabilities.isCreativeMode) {
                    final ItemStack toAdd = new ItemStack(cache.getItem(), cache.getItem().getItemStackLimit(), cache
                            .getMetadata());

                    int addStackSize = toAdd.stackSize;
                    final ItemStack remaining = ((CachesTileEntity) te).mergeStackIntoSlot(toAdd);
                    int printoutStackSize = addStackSize;
                    if (remaining != null) {
                        printoutStackSize = addStackSize - remaining.stackSize;
                    }

                    if (printoutStackSize == 0) {
                        player.addChatComponentMessage(new ChatComponentText("Cannot add more to cache as it is full!"));
                    }
                } else {
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
                            int printoutStackSize = slotStackSize;
                            if (remaining == null) {
                                player.inventory.setInventorySlotContents(i, null);
                            } else {
                                printoutStackSize = slotStackSize - remaining.stackSize;
                            }

                            if (printoutStackSize == 0) {
                                player.addChatComponentMessage(new ChatComponentText("Cannot add more to cache as it is full!"));
                            }

                            if (!depositAll) {
                                break;
                            }
                        }
                    }
                }

                te.markDirty();
            }

            player.openContainer.detectAndSendChanges();
        }
        return true;
    }

    @Override
    public void onBlockClicked(World worldIn, int x, int y, int z, EntityPlayer player) {
        if (!worldIn.isRemote) {
            this.handleLeftClickBlock(worldIn, player, x, y, z);
        }
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, int x, int y, int z, int meta) {
        super.harvestBlock(worldIn, player, x, y, z, meta);
        worldIn.setBlockToAir(x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        if (!worldIn.isRemote) {
            int l = MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 2.5D) & 3;
            worldIn.setBlockMetadataWithNotify(x, y, z, l, 2);

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
                            cache = CachesTileEntity.loadItemStackFromNBT(cachesCompound.getCompoundTag(CachesTileEntity.TAG_CACHE_CONTENTS));
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
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.tb = new ExternalIcon(getTextureName() + "_tb", FileSystem.CONFIG_IMAGES_PATH).register((TextureMap) reg);
        this.side = new ExternalIcon(getTextureName() + "_side", FileSystem.CONFIG_IMAGES_PATH).register((TextureMap) reg);
        this.front = new ExternalIcon(getTextureName() + "_front", FileSystem.CONFIG_IMAGES_PATH).register((TextureMap) reg);
    }

    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        //mmmmm Pumpkin Pie
        return side == 1 ? this.tb : (side == 0 ? this.tb : (meta == 2 && side == 2 ? this.front : (meta == 3 && side == 5 ? this.front
                : (meta == 0 && side == 3 ? this.front : (meta == 1 && side == 4 ? this.front : this.side)))));
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (!world.isRemote) {
            // Here commences our traditional circus music...
            if (player.capabilities.isCreativeMode) {
                this.handleLeftClickBlock(world, player, x, y, z);
            }
        }

        // Handle creative removal here
        if (player.capabilities.isCreativeMode && player.isSneaking()) {
            world.setBlockToAir(x, y, z);
            return true;
        }

        // We return true to let Vanilla know "Yes it was removed" but the actual removal and item drop happens in harvestBlock
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new CachesTileEntity(this.cacheLimit);
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
        return this.cacheLimit;
    }

    public String getDisplayName() {
        return displayName;
    }

    private boolean handleLeftClickBlock(World world, EntityPlayer player, int x, int y, int z) {
        final TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof CachesTileEntity)) {
            return true;
        }
        
        if (player.isSneaking() && player.getHeldItem()!=null && player.getHeldItem().getItem() == Item.itemRegistry.getObject("redstone")) {
            return false;  // Specifically added for SignShop compatibility.
        }
              
        final ItemStack cache = ((CachesTileEntity) te).getCache();

        if (cache != null) {

            final int maxItemStackSize = cache.getItem().getItemStackLimit();
            final int cacheStackSize = cache.stackSize;
            final int inventoryMaxStackSize = player.inventory.getInventoryStackLimit();

            final int stackSize = player.isSneaking() ? 1 : maxItemStackSize < cacheStackSize ? maxItemStackSize : cacheStackSize > inventoryMaxStackSize ? inventoryMaxStackSize : cacheStackSize;

            ItemStack toAdd = new ItemStack(cache.getItem(), stackSize, cache.getMetadata());
            if (cache.getTagCompound() != null) {
                toAdd.setTagCompound((NBTTagCompound) cache.getTagCompound().copy());
            }

            int preMerge = toAdd.stackSize;

            if (!player.inventory.addItemStackToInventory(toAdd)) {
                player.addChatComponentMessage(new ChatComponentText("Cannot withdrawal from cache as your inventory is full!"));
                return true;
            } else {
                ItemStack newCache = cache;
                newCache.stackSize -= (preMerge - toAdd.stackSize);

                if (newCache.stackSize == 0) {
                    newCache = null;
                }

                ((CachesTileEntity) te).actuallySetInventorySlotContent(0, newCache);
            }
        }

        return false;
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        // Removal of TE is delayed to harvestBlock
    }
}
