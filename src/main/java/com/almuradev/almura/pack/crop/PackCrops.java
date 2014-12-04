/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.crop.stage.Stage;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.BlockCrops;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class PackCrops extends BlockCrops implements IPackObject, IBlockClipContainer, IBlockShapeContainer {

    public static int renderId;
    private final ContentPack pack;
    private final int levelRequired;
    private final Map<Integer, Stage> stages = Maps.newHashMap();

    public PackCrops(ContentPack pack, String identifier, String textureName, int levelRequired) {
        this.pack = pack;
        this.levelRequired = levelRequired;
        setBlockName(pack.getName() + "_" + identifier);
        setBlockTextureName(Almura.MOD_ID.toLowerCase() + ":images/" + textureName);
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        final int metadata = world.getBlockMetadata(x, y, z);
        //TODO Needs serious testing
        if (metadata >= stages.values().size() - 1) {
            setTickRandomly(false);
            return;
        }
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            stage.onTick(world, x, y, z, random);
            //Get within range
            final double chance = stage.growth.minChance + (stage.growth.maxChance - stage.growth.minChance) * random.nextDouble();

            if (random.nextDouble() <= 100 / chance) {
                stage.onGrowth(world, x, y, z, random);
                world.setBlockMetadataWithNotify(x, y, z, metadata + 1, 3);
                final Stage newStage = stages.get(metadata + 1);
                newStage.onGrown(world, x, y, z, random);
            }
        }
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            return stage.getDrops(world, x, y, z, metadata, fortune);
        }
        return Lists.newArrayList();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = new PackIcon(pack.getName(), textureName).register((TextureMap) register);
        for (Stage stage : stages.values()) {
            stage.registerBlockIcons(blockIcon, textureName, register);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        final Stage stage = stages.get(type);
        final IIcon icon = super.getIcon(side, type);

        return stage != null ? stage.getIcon(icon, side, type) : icon;
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        final int metadata = world.getBlockMetadata(x, y, z);
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            final PackShape shape = stage.getShape(world, x, y, z, metadata);
            if (shape != null && !shape.useVanillaCollision) {
                return shape.getCollisionBoundingBoxFromPool(vanillaBB, world, x, y, z);
            }
        }
        return vanillaBB;
    }

    /**
     * Returns the bounding box of the wired rectangular prism to render.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getSelectedBoundingBoxFromPool(world, x, y, z);
        final int metadata = world.getBlockMetadata(x, y, z);
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            final PackShape shape = stage.getShape(world, x, y, z, metadata);
            if (shape != null && !shape.useVanillaWireframe) {
                return shape.getSelectedBoundingBoxFromPool(vanillaBB, world, x, y, z);
            }
        }
        return vanillaBB;
    }

    @Override
    public ContentPack getPack() {
        return pack;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        final Stage stage = stages.get(0);
        return stage == null ? null : stage.getClipIcons();
    }

    @Override
    public ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata) {
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            return stage.getClipIcons(access, x, y, z, metadata);
        }
        return new ClippedIcon[0];
    }

    @Override
    public PackShape getShape(IBlockAccess access, int x, int y, int z, int metadata) {
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            return stage.getShape(access, x, y, z, metadata);
        }
        return null;
    }

    @Override
    public PackShape getShape() {
        final Stage stage = stages.get(0);
        return stage == null ? null : stage.getShape();
    }

    @Override
    public void refreshShape() {
        for (Stage stage : stages.values()) {
            stage.refreshShape();
        }
    }

    public int getLevelRequired() {
        return levelRequired;
    }
}
