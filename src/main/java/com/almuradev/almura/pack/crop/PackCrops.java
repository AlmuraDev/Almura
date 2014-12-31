/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.node.GrowthNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.RenderNode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
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
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

public class PackCrops extends BlockCrops implements IPackObject, IBlockClipContainer, IBlockShapeContainer, INodeContainer {

    public static int renderId;
    private final Pack pack;
    private final String identifier;
    private final int levelRequired;
    private final String textureName;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final Map<Integer, Stage> stages;

    public PackCrops(Pack pack, String identifier, String textureName, int levelRequired, Map<Integer, Stage> stages) {
        this.pack = pack;
        this.identifier = identifier;
        this.levelRequired = levelRequired;
        this.stages = stages;
        this.textureName = textureName;
        setBlockName(pack.getName() + "\\" + identifier);
        setBlockTextureName(Almura.MOD_ID + ":images/" + textureName);
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        final int metadata = world.getBlockMetadata(x, y, z);
        //TODO Needs serious testing
        if (metadata >= stages.size() - 1) {
            return;
        }
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            stage.onTick(world, x, y, z, random);
            //Get within range
            final double
                    chance =
                    stage.getNode(GrowthNode.class).getValue().getValueWithinRange();
            if (random.nextDouble() <= (chance / 100)) {
                stage.onGrowth(world, x, y, z, random);
                final Stage newStage = stages.get(metadata + 1);
                newStage.onGrown(world, x, y, z, random);
                world.setBlockMetadataWithNotify(x, y, z, metadata + 1, 3);
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
    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        final Stage stage = stages.get(world.getBlockMetadata(x, y, z));
        return stage != null ? stage.getLightOpacity(world, x, y, z) : super.getLightOpacity(world, x, y, z);
    }

    @Override
    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        final Stage stage = stages.get(metadata);
        return stage != null ? stage.getExpDrop(world, metadata, fortune) : super.getExpDrop(world, metadata, fortune);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = new PackIcon(this, textureName).register((TextureMap) register);
        for (Stage stage : stages.values()) {
            stage.registerBlockIcons(blockIcon, textureName, register);
        }
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        final Stage stage = stages.get(world.getBlockMetadata(x, y, z));
        return stage != null ? stage.getLightValue(world, x, y, z) : super.getLightValue(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        final Stage stage = stages.get(type);

        return stage != null ? stage.getIcon(blockIcon, side, type) : blockIcon;
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
    public Pack getPack() {
        return pack;
    }

    @Override
    public String getIdentifier() {
        return identifier;
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
    public void setShape(PackShape shape) {
        for (Stage stage : stages.values()) {
            stage.setShape(shape);
        }
    }

    @Override
    public String getShapeName() {
        return "";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T addNode(T node) {
        nodes.put((Class<? extends INode<?>>) node.getClass(), node);
        MinecraftForge.EVENT_BUS.post(new AddNodeEvent(this, node));
        return node;
    }

    @Override
    public void addNodes(INode<?>... nodes) {
        for (INode<?> node : nodes) {
            addNode(node);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T getNode(Class<T> clazz) {
        return (T) nodes.get(clazz);
    }

    @Override
    public <T extends INode<?>> boolean hasNode(Class<T> clazz) {
        return getNode(clazz) != null;
    }

    public int getLevelRequired() {
        return levelRequired;
    }

    public Map<Integer, Stage> getStages() {
        return Collections.unmodifiableMap(stages);
    }

    @Override
    public String toString() {
        return "PackCrops {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + ", stages= " + stages + "}";
    }
}
