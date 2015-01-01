/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.item;

import com.almuradev.almura.Tabs;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.node.ConsumptionNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class PackFood extends ItemFood implements IPackObject, IClipContainer, IShapeContainer, INodeContainer {

    private final Pack pack;
    private final String identifier;
    private final Map<Integer, List<Integer>> textureCoordinates;
    private final String shapeName;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final ConsumptionNode consumption;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;
    private List<String> tooltip;

    public PackFood(Pack pack, String identifier, List<String> tooltip, String textureName, String shapeName,
                    Map<Integer, List<Integer>> textureCoordinates, boolean showInCreativeTab, String creativeTabName,
                    ConsumptionNode consumptionNode) {
        super(-1, -1, consumptionNode.isWolfFavorite());
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinates = textureCoordinates;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.tooltip = tooltip;
        consumption = addNode(consumptionNode);
        setUnlocalizedName(pack.getName() + "\\" + identifier);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
        if (consumption.isAlwaysEdible()) {
            setAlwaysEdible();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        for (String str : tooltip) {
            list.add(str);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        if (PackUtil.isEmptyClip(clippedIcons)) {
            return super.getIconFromDamage(p_77617_1_);
        }
        return clippedIcons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = new PackIcon(this, textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoordinates(itemIcon, textureName, textureCoordinates);
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        final ItemStack result = super.onEaten(stack, world, player);
        if (!world.isRemote) {
            player.heal(consumption.getHealthRange().getValueWithinRange());
        }
        return result;
    }

    @Override
    public int func_150905_g(ItemStack stack) {
        return consumption.getFoodRange().getValueWithinRange();
    }

    @Override
    public float func_150906_h(ItemStack stack) {
        return consumption.getSaturationRange().getValueWithinRange();
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

    @Override
    public String toString() {
        return "PackFood {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }
}
