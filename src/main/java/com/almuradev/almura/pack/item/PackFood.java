/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.item;

import com.almuradev.almura.Tabs;
import com.almuradev.almura.pack.IRecipeContainer;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.renderer.PackIcon;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PackFood extends ItemFood implements IPackObject, IClipContainer, IShapeContainer, IRecipeContainer {

    private final Pack pack;
    private final String identifier;
    //TEXTURES
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    //SHAPES
    private final String shapeName;
    public ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;
    private String[] tooltip;
    private final boolean hasRecipe;

    public PackFood(Pack pack, String identifier, String[] tooltip, String textureName, String shapeName,
                    Map<Integer, List<Integer>> textureCoordinatesByFace,
                    boolean showInCreativeTab, String creativeTabName, int healAmount, float saturationModifier, boolean isWolfFavorite,
                    boolean alwaysEdible, boolean hasRecipe) {
        super(healAmount, saturationModifier, isWolfFavorite);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.tooltip = tooltip;
        this.hasRecipe = hasRecipe;
        setUnlocalizedName(pack.getName() + "\\" + identifier);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
        if (alwaysEdible) {
            setAlwaysEdible();
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        if (tooltip != null && tooltip.length > 0) {
            Collections.addAll(list, tooltip);
        }
    }

    @Override
    public void registerIcons(IIconRegister register) {
        itemIcon = new PackIcon(this, textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoords(itemIcon, textureName, textureCoordinatesByFace);
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
        return "PackFood {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }

    @Override
    public boolean hasRecipe() {
        return hasRecipe;
    }
}
