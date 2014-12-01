/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class PackCrops extends BlockCrops implements IPackObject {
    public static final Random RANDOM = new Random();

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

    private final ContentPack pack;
    private final String textureName;
    private final LinkedHashMap<Integer, Stage> stages;
    private Block farmland;
    private Item seed;

    public PackCrops(ContentPack pack, String identifier, String textureName, LinkedHashMap<Integer, Stage> stages) {
        this.pack = pack;
        this.textureName = textureName;
        this.stages = stages == null ? Maps.<Integer, Stage>newLinkedHashMap() : stages;
        setBlockName(Almura.MOD_ID + ":" + pack.getName() + "/" + identifier);
        setBlockTextureName(Almura.MOD_ID.toLowerCase() + ":images/" + textureName);

        setTickRandomly(true);
        this.setCreativeTab(null);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        // Todo: what to do in update tick
    }

    @Override
    public boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_) {

    }

    @Override
    protected boolean canPlaceBlockOn(Block block) {
        return block == farmland;
    }

    @Override
    public int getRenderType() {
        return 6; //BlockCrops X renderer
    }

    @Override
    protected Item func_149866_i() {  //Seed Drops on Break
        return seed;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) { // Calculate drops when using enchanted tools
        if (metadata <= 15) {
            final Stage stage = stages.get(metadata);

            if (stage != null) {
                return stage.getDrops(fortune);
            }
        }
    }

    @Override
    public void func_149853_b(World world, Random random, int x, int y, int z) { // current growth rate?
        this.func_149863_m(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = new PackIcon(pack.getName(), textureName).register((TextureMap) register);

        for (Map.Entry<Integer, Stage> entry : stages.entrySet()) {
            entry.getValue().registerBlockIcons(blockIcon, Paths.get(Filesystem.CONFIG_IMAGES_PATH.toString(), textureName + ".png"), textureName);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        final Stage stage = stages.get(type);
        if (stage == null) {
            return super.getIcon(side, type);
        }

        if (PackUtil.isEmpty(stage.clippedIcons)) {
            return super.getIcon(side, type);
        }

        ClippedIcon sideIcon;

        if (side >= stage.clippedIcons.length) {
            sideIcon = stage.clippedIcons[0];
        } else {
            sideIcon = stage.clippedIcons[side];

            if (sideIcon == null) {
                sideIcon = stage.clippedIcons[0];
            }
        }

        return sideIcon;
    }

    @Override
    public ContentPack getPack() {
        return pack;
    }

    public void setFarmland(Block farmland) {
        this.farmland = farmland;
    }
}
