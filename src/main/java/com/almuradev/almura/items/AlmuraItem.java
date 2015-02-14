/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ExternalIcon;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class AlmuraItem extends Item implements IPackObject {

    /**
     * Creates an item that that registers the name (in ENGLISH_AMERICAN) and registers the item in the game registry.
     *
     * @param unlocalizedName the unlocalized name
     * @param displayName     the localized english american name
     * @param textureName     the texture name
     */
    public AlmuraItem(String unlocalizedName, String displayName, String textureName) {
        this(unlocalizedName, displayName, textureName, null);
    }

    /**
     * Creates an item that that registers the name (in ENGLISH_AMERICAN) and registers the item in the game registry.
     * This also sets the creative tab.
     *
     * @param unlocalizedName the unlocalized name
     * @param displayName     the localized english american name
     * @param textureName     the texture name
     * @param creativeTab     the creative tab
     */
    public AlmuraItem(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
        setUnlocalizedName(unlocalizedName);
        setTextureName(Almura.MOD_ID + ":internal/items/" + textureName);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, getUnlocalizedName() + ".name", displayName);
        if (creativeTab != null) {
            setCreativeTab(creativeTab);
        }
        GameRegistry.registerItem(this, unlocalizedName.replace(".", "\\"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = new ExternalIcon(getIconString()).register((TextureMap) register);
    }

    @Override
    public Pack getPack() {
        return Almura.INTERNAL_PACK;
    }

    @Override
    public String getIdentifier() {
        return getUnlocalizedName();
    }
}
