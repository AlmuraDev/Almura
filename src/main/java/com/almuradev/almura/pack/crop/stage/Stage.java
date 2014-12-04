/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage;

import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IState;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.crop.stage.property.Growth;
import com.almuradev.almura.pack.model.PackShape;
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
    public final int id;
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    private final String shapeName;
    private PackShape shape;
    private ClippedIcon[] clippedIcons;
    //PROPERTY
    public Growth growth;

    public Stage(PackCrops block, int id, Map<Integer, List<Integer>> textureCoordinatesByFace, String shapeName, Growth growth) {
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
    public ContentPack getPack() {
        return block.getPack();
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
    public void refreshShape() {
        this.shape = null;

        if (shapeName != null) {
            for (PackShape shape : ContentPack.getShapes()) {
                if (shape.getName().equals(shapeName)) {
                    this.shape = shape;
                    break;
                }
            }
        }
        if (shape != null) {
            if (!shape.useVanillaCollision) {
                block.setBlockBounds(shape.collisionCoordinates.get(0).floatValue(), shape.collisionCoordinates.get(1).floatValue(),
                                     shape.collisionCoordinates.get(2).floatValue(), shape.collisionCoordinates.get(3).floatValue(),
                                     shape.collisionCoordinates.get(4).floatValue(), shape.collisionCoordinates.get(5).floatValue());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIcon blockIcon, String textureName, IIconRegister register) {
        clippedIcons = PackUtil.generateClippedIconsFromCoords(blockIcon, textureName, textureCoordinatesByFace);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IIcon blockIcon, int side, int type) {
        if (PackUtil.isEmpty(clippedIcons)) {
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
}
