package com.almuradev.almura.pack.chest;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.Random;

public class PackChest extends BlockChest {

    public final int field_149956_a;
    private final Random field_149955_b = new Random();

    protected PackChest(int value) {
        super(value);
        this.field_149956_a = value;
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
    }

    private static boolean func_149953_o(World world, int x, int y, int z) {  // Todo: no idea wtf this is.
        Iterator
                iterator =
                world.getEntitiesWithinAABB(EntityOcelot.class, AxisAlignedBB
                        .getBoundingBox((double) x, (double) (y + 1), (double) z, (double) (x + 1), (double) (y + 2), (double) (z + 1))).iterator();
        EntityOcelot entityocelot;

        do {
            if (!iterator.hasNext()) {
                return false;
            }
            Entity entity = (Entity) iterator.next();
            entityocelot = (EntityOcelot) entity;
        } while (!entityocelot.isSitting());
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }

    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess iblock, int x, int y, int z) {
        this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        //Todo: determine what to do.
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
        //Todo: determine what to do.
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return true; //Todo: determine when it is not ok to place this custom block.
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        // Todo: determine what to do.
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
        TileEntityChest tileentitychest = (TileEntityChest) world.getTileEntity(x, y, z);

        if (tileentitychest != null) {
            for (int i1 = 0; i1 < tileentitychest.getSizeInventory(); ++i1) {
                ItemStack itemstack = tileentitychest.getStackInSlot(i1);

                if (itemstack != null) {
                    float f = this.field_149955_b.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.field_149955_b.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem;

                    for (float f2 = this.field_149955_b.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; world.spawnEntityInWorld(entityitem)) {
                        int j1 = this.field_149955_b.nextInt(21) + 10;

                        if (j1 > itemstack.stackSize) {
                            j1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= j1;
                        entityitem =
                                new EntityItem(world, (double) ((float) x + f), (double) ((float) y + f1), (double) ((float) z + f2),
                                               new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
                        float f3 = 0.05F;
                        entityitem.motionX = (double) ((float) this.field_149955_b.nextGaussian() * f3);
                        entityitem.motionY = (double) ((float) this.field_149955_b.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double) ((float) this.field_149955_b.nextGaussian() * f3);

                        if (itemstack.hasTagCompound()) {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                        }
                    }
                }
            }

            world.func_147453_f(x, y, z, block);
        }

        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_,
                                    float p_149727_9_) {
        if (world.isRemote) {
            return true; //Need server code for this part??? @Zidane
        } else {
            IInventory iinventory = this.func_149951_m(world, x, y, z);
            if (iinventory != null) {
                player.displayGUIChest(iinventory);
            }
            return true;
        }
    }

    @Override
    public IInventory func_149951_m(World world, int x, int y, int z) {
        Object object = (TileEntityChest) world.getTileEntity(x, y, z);

        if (object == null) {
            return null;
        } else if (world.isSideSolid(x, y + 1, z, DOWN)) {
            return null;
        } else if (func_149953_o(world, x, y, z)) {
            return null;
        } else if (world.getBlock(x - 1, y, z) == this && (world.isSideSolid(x - 1, y + 1, z, DOWN) || func_149953_o(world, x - 1, y, z))) {
            return null;
        } else if (world.getBlock(x + 1, y, z) == this && (world.isSideSolid(x + 1, y + 1, z, DOWN) || func_149953_o(world, x + 1, y, z))) {
            return null;
        } else if (world.getBlock(x, y, z - 1) == this && (world.isSideSolid(x, y + 1, z - 1, DOWN) || func_149953_o(world, x, y, z - 1))) {
            return null;
        } else if (world.getBlock(x, y, z + 1) == this && (world.isSideSolid(x, y + 1, z + 1, DOWN) || func_149953_o(world, x, y, z + 1))) {
            return null;
        } else {
            if (world.getBlock(x - 1, y, z) == this) {
                object = new InventoryLargeChest("container.chestDouble", (TileEntityChest) world.getTileEntity(x - 1, y, z), (IInventory) object);
            }

            if (world.getBlock(x + 1, y, z) == this) {
                object = new InventoryLargeChest("container.chestDouble", (IInventory) object, (TileEntityChest) world.getTileEntity(x + 1, y, z));
            }

            if (world.getBlock(x, y, z - 1) == this) {
                object = new InventoryLargeChest("container.chestDouble", (TileEntityChest) world.getTileEntity(x, y, z - 1), (IInventory) object);
            }

            if (world.getBlock(x, y, z + 1) == this) {
                object = new InventoryLargeChest("container.chestDouble", (IInventory) object, (TileEntityChest) world.getTileEntity(x, y, z + 1));
            }

            return (IInventory) object;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister icon) {
        this.blockIcon = icon.registerIcon("planks_oak");
    }

    @Override
    public TileEntity createNewTileEntity(World notUsedWorld, int notUsedInt) {
        TileEntityChest tileentitychest = new TileEntityChest();
        return tileentitychest;
    }

}
