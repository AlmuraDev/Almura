/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.tabs;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.util.ExternalIcon;
import com.almuradev.almura.util.FileSystem;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;

public class AlmuraTabItem extends Item {

    public AlmuraTabItem(String name) {
        setUnlocalizedName(name);
        setTextureName(Almura.PLUGIN_ID + ":" + name);
        setMaxStackSize(1);
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void registerIcons(IIconRegister register) {
        itemIcon = new ExternalIcon(getIconString(), FileSystem.CONFIG_IMAGES_PATH).register((TextureMap) register);
    }
}
