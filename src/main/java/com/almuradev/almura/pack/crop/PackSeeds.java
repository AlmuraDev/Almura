/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.node.GrassNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class PackSeeds extends ItemSeeds implements IPackObject, IClipContainer, IShapeContainer, INodeContainer {

    private final Pack pack;
    private final String identifier;
    private final Map<Integer, List<Integer>> textureCoordinates;
    private final String shapeName;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final List<String> tooltip;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;

    public PackSeeds(Pack pack, String identifier, List<String> tooltip, String textureName, String shapeName,
                     Map<Integer, List<Integer>> textureCoordinates, boolean showInCreativeTab, String creativeTabName, Block crop, Block soil) {
        super(crop, soil);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinates = textureCoordinates;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.tooltip = tooltip;
        setUnlocalizedName(pack.getName() + "\\" + identifier);
        setTextureName(Almura.MOD_ID + ":images/" + textureName);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        for (String str : tooltip) {
            list.add(str);
        }
    }

    @Override
    public IIcon getIconFromDamage(int p_77617_1_) {
        if (PackUtil.isEmptyClip(clippedIcons)) {
            return super.getIconFromDamage(p_77617_1_);
        }
        return clippedIcons[0];
    }

    @Override
    public void registerIcons(IIconRegister register) {
        itemIcon = new PackIcon(this, textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoordinates(itemIcon, textureName, textureCoordinates);
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
        return clippedIcons;
    }

    @Override
    public PackShape getShape() {
        return shape;
    }

    @Override
    public void setShape(PackShape shape) {
        this.shape = shape;
    }

    @Override
    public String getShapeName() {
        return shapeName;
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

    @SubscribeEvent
    public void onAddNodeEvent(AddNodeEvent event) {
        if (event.getNode() instanceof GrassNode) {
            if (((GrassNode) event.getNode()).isEnabled()) {
                MinecraftForge.addGrassSeed(new ItemStack(this, 1, 0), 0);
            }
        }
    }

    @Override
    public String toString() {
        return "PackSeeds {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }
}
