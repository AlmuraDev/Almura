/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import com.almuradev.almura.pack.IBlockTextureContainer;
import com.almuradev.almura.pack.IBlockModelContainer;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IState;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.pack.node.GrowthNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

public class Stage implements IState, IPackObject, IBlockTextureContainer, IBlockModelContainer, INodeContainer {

    private final PackCrops block;
    private final int id;
    private Map<Integer, List<Integer>> textureCoordinatesByFace;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final String modelName;
    private Optional<PackModelContainer> modelContainer;
    private ClippedIcon[] clippedIcons;

    public Stage(PackCrops block, int id, Map<Integer, List<Integer>> textureCoordinatesByFace, String modelName, PackModelContainer modelContainer,
            GrowthNode growth,
            LightNode light) {
        this.block = block;
        this.id = id;
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.modelName = modelName;
        setModelContainer(modelContainer);
        addNode(growth);
        addNode(light);
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
    public Map<Integer, List<Integer>> getTextureCoordinates() {
        return textureCoordinatesByFace;
    }

    @Override public void setTextureCoordinates(Map<Integer, List<Integer>> coordinates) {
        this.textureCoordinatesByFace = coordinates;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @Override
    public Optional<PackModelContainer> getModelContainer(IBlockAccess access, int x, int y, int z, int metadata) {
        return modelContainer;
    }

    @Override
    public Optional<PackModelContainer> getModelContainer() {
        return modelContainer;
    }

    @Override
    public void setModelContainer(PackModelContainer modelContainer) {
        this.modelContainer = Optional.fromNullable(modelContainer);
    }

    @Override
    public String getModelName() {
        return modelName;
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
     *
     * @param world  the world
     * @param x      x coordinate
     * @param y      y coordinate
     * @param z      z coordinate
     * @param random random
     */
    public void onGrowth(World world, int x, int y, int z, Random random) {

    }

    /**
     * Called when a stage has achieved growth (the stage has become the current stage)
     *
     * @param world  the world
     * @param x      x coordinate
     * @param y      y coordinate
     * @param z      z coordinate
     * @param random random
     */
    public void onGrown(World world, int x, int y, int z, Random random) {
        if (!world.isRemote) {
            float d0 = 0.02f;
            float d1 = 0.02f;
            float d2 = 0.02f;

            MinecraftServer.getServer().getConfigurationManager().sendToAllNear(x, y, z, 50D, world.provider.dimensionId,
                    new S2APacketParticles("happyVillager", (x + random.nextFloat()),
                            (float) (y + random.nextFloat() * block
                                    .getBlockBoundsMaxY()),
                            (z + random.nextFloat()), d0, d1, d2, 1, id));
        }
    }

    /**
     * Called when {@link PackCrops} is ticked. Never called for the final stage.
     *
     * @param world  the world
     * @param x      x coordinate
     * @param y      y coordinate
     * @param z      z coordinate
     * @param random random
     */
    public void onTick(World world, int x, int y, int z, Random random) {

    }

    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        return getNode(LightNode.class).getOpacity();
    }

    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        return Integer.MIN_VALUE;
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return (int) getNode(LightNode.class).getEmission();
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

    @Override
    public String toString() {
        return "Stage {id= " + id + "}";
    }
}
