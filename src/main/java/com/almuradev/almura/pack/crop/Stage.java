/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IState;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.node.GrowthNode;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Stage implements IState, IPackObject, IBlockClipContainer, IBlockShapeContainer {

    private final PackCrops block;
    private final int id;
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    private final String shapeName;
    //PROPERTY
    public GrowthNode growth;
    private PackShape shape;
    private ClippedIcon[] clippedIcons;

    public Stage(PackCrops block, int id, Map<Integer, List<Integer>> textureCoordinatesByFace, String shapeName, GrowthNode growth) {
        this.block = block;
        this.id = id;
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.shapeName = shapeName;
        this.growth = growth;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Pack getPack() {
        return block.getPack();
    }

    @Override
    public String getIdentifier() {
        return block.getIdentifier() + "_stage_" + id;
    }

    @Override
    public ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata) {
        return clippedIcons;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @Override
    public PackShape getShape(IBlockAccess access, int x, int y, int z, int metadata) {
        return shape;
    }

    @Override
    public PackShape getShape() {
        return shape;
    }

    @Override
    public void setShape(PackShape shape) {
        for (PackShape s : Pack.getShapes()) {
            if (s.getName().equalsIgnoreCase(shapeName)) {
                this.shape = s;
                break;
            }
        }
        if (shape != null) {
            if (!shape.useVanillaRenderBounds) {
                block.setBlockBounds(shape.renderBoundsCoordinates.get(0).floatValue(), shape.renderBoundsCoordinates.get(1).floatValue(),
                                     shape.renderBoundsCoordinates.get(2).floatValue(), shape.renderBoundsCoordinates.get(3).floatValue(),
                                     shape.renderBoundsCoordinates.get(4).floatValue(), shape.renderBoundsCoordinates.get(5).floatValue());
            }
        }
    }

    @Override
    public String getShapeName() {
        return shapeName;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIcon blockIcon, String textureName, IIconRegister register) {
        clippedIcons = PackUtil.generateClippedIconsFromCoordinates(blockIcon, textureName, textureCoordinatesByFace);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IIcon blockIcon, int side, int type) {
        if (PackUtil.isEmptyClip(clippedIcons)) {
            return blockIcon;
        }
        ClippedIcon sideIcon;

        if (side >= clippedIcons.length) {
            sideIcon = clippedIcons[0];
        } else {
            sideIcon = clippedIcons[side];

            if (sideIcon == null) {
                sideIcon = clippedIcons[0];
            }
        }

        return sideIcon;
    }

    /**
     * Called when a stage is growing into another stage
     * @param world the world
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param random random
     */
    public void onGrowth(World world, int x, int y, int z, Random random) {

    }

    /**
     * Called when a stage has achieved growth (the stage has become the current stage)
     * @param world the world
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param random random
     */
    public void onGrown(World world, int x, int y, int z, Random random) {

    }

    /**
     * Called when {@link PackCrops} is ticked. Never called for the final stage.
     * @param world the world
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param random random
     */
    public void onTick(World world, int x, int y, int z, Random random) {

    }

    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return Lists.newArrayList();
    }

    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        return Integer.MIN_VALUE;
    }

    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        return Integer.MIN_VALUE;
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return Integer.MIN_VALUE;
    }
}
