/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.item;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.PackIcon;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;

import java.util.List;
import java.util.Map;

public class PackItem extends Item {

    private final ContentPack pack;
    //TEXTURES
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    //SHAPES
    private final String shapeName;
    public ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;

    public PackItem(ContentPack pack, String identifier, String textureName, String shapeName, Map<Integer, List<Integer>> textureCoordinatesByFace,
                    boolean showInCreativeTab, String creativeTabName) {
        this.pack = pack;
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.textureName = textureName;
        this.shapeName = shapeName;

        setUnlocalizedName(pack.getName() + "_" + identifier);
        setTextureName(Almura.MOD_ID.toLowerCase() + ":packs/" + pack.getName() + "_" + textureName);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }

        GameRegistry.registerItem(this, pack.getName() + "_" + identifier);

        if (Configuration.IS_CLIENT) {
            Almura.SHAPE_RENDERER.registerFor(this);
        }
    }

    public static PackItem createFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild("Title").getString(name);
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final boolean showInCreativeTab = reader.getChild("show-in-creative-tab").getBoolean(true);
        final String creativeTabName = reader.getChild("creative-tab-name").getString("legacy");

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(reader);

        Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "_" + name + ".name", title);

        return new PackItem(pack, name, textureName, shapeName, textureCoordinatesByFace, showInCreativeTab, creativeTabName);
    }

    @Override
    public void registerIcons(IIconRegister register) {
        itemIcon = new PackIcon(pack.getName(), textureName).register((TextureMap) register);

        if (textureCoordinatesByFace.isEmpty()) {
            return;
        }

        clippedIcons = PackUtil.generateClippedIconsFromCoords(pack, itemIcon, textureName, textureCoordinatesByFace);
    }

    public ContentPack getPack() {
        return pack;
    }

    public PackShape getShape() {
        return shape;
    }

    public void reloadShape() {
        this.shape = null;

        if (shapeName != null) {
            for (PackShape shape : pack.getShapes()) {
                if (shape.getName().equals(shapeName)) {
                    this.shape = shape;
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "PackItem {pack= " + pack.getName() + ", raw_name= " + getUnlocalizedName() + "}";
    }
}
