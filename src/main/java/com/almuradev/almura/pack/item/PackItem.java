/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.item;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.renderer.PackIcon;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public class PackItem extends Item implements IPackObject, IClipContainer, IShapeContainer {

    private final Pack pack;
    private final String identifier;
    private final Map<Integer, List<Integer>> textureCoordinates;
    private final String shapeName;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;
    private List<String> tooltip;

    public PackItem(Pack pack, String identifier, List<String> tooltip, String textureName, String shapeName,
                    Map<Integer, List<Integer>> textureCoordinates, boolean showInCreativeTab, String creativeTabName) {
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
    public String toString() {
        return "PackItem {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }
}
