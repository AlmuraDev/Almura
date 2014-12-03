/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class PackCrop extends BlockCrops implements IGrowable {
    // Constructor Object Needs:
    //  a. seed
    //  b. amount of stages (int)
    //  c. minimum light to grow
    //  d. maximum light to grow
    //  e. growth rate
    //  f. drop type (itemStack)
    //  g. drop quantity (int)
    //  h. drop bonus (itemStack)
    //  i. bonus quantity (int)
    //  j. water required to grow (boolean)
    //  k. water range scan (int)
    //  l. fertilizer type (itemStack)
    //  m. damage player
    //  n. drop seed on grass break (boolean)

    private IIcon[] cropIcon;

    protected PackCrop() {
        this.setTickRandomly(true);
        float f = 0.5F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f); // small base block, ok for now.
        this.setCreativeTab((CreativeTabs) null); // needs to be configurable
        this.setHardness(0.0F); // Needs to be in configurable
        this.setStepSound(soundTypeGrass); // needs to be configurable        
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        // Todo: what to do in update tick
    }

    private float getGrowthRate(World world, int x, int y, int z) {
        // Todo: determine growth rate.
        return 0;
    }

    @Override
    protected boolean canPlaceBlockOn(Block block) {
        // Todo: specify which type of block the crop can be planted at.
        return block == Blocks.farmland;
    }

    @Override
    public int getRenderType() {
        return 6; //BlockCrops X renderer
    }

    @Override
    protected Item func_149866_i() {  //Seed Drops on Break
        return Items.wheat_seeds;
    }

    @Override
    protected Item func_149865_P() { // Crop drop for completed crop on break.
        return Items.wheat;
    }

    @Override
    public Item getItemDropped(int growthValueMetaData, Random notUsedRandom, int notUsedInt) { // Determine items to be dropped
        return growthValueMetaData == 7 ? this.func_149865_P()
                                        : this.func_149866_i(); // Metadata 7 = final growth rate of finished crop.  Must be configurable.
    }

    @Override
    public int quantityDropped(Random p_149745_1_) {
        return 1; // must be configurable.
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Item getItem(World world, int x, int y, int z) { //Todo: determine wtf this is.
        return this.func_149866_i(); //returns seed.
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) { // Calculate drops when using enchanted tools
        ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);

        if (metadata >= 7) {
            for (int i = 0; i < 3 + fortune; ++i) {
                if (world.rand.nextInt(15) <= metadata) {
                    ret.add(new ItemStack(this.func_149866_i(), 1, 0));
                }
            }
        }
        return ret;
    }

    @Override
    public void func_149853_b(World world, Random random, int x, int y, int z) { // current growth rate?
        this.func_149863_m(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister p_149651_1_) {
        this.cropIcon = new IIcon[8];
        for (int i = 0; i < this.cropIcon.length; ++i) {
            this.cropIcon[i] = p_149651_1_.registerIcon(this.getTextureName() + "_stage_" + i);
        }
    }
}
