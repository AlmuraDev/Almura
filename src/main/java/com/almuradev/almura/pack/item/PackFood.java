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
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.PackIcon;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.common.registry.GameRegistry;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemFood;

import java.util.List;
import java.util.Map;

public class PackFood extends ItemFood implements IClipContainer, IShapeContainer {

    private final ContentPack pack;
    //TEXTURES
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    //SHAPES
    private final String shapeName;
    public ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;

    public PackFood(ContentPack pack, String identifier, String textureName, String shapeName, Map<Integer, List<Integer>> textureCoordinatesByFace,
                    boolean showInCreativeTab, String creativeTabName, int healAmount, float saturationModifier, boolean isWolfFavorite,
                    boolean alwaysEdible) {
        super(healAmount, saturationModifier, isWolfFavorite);
        this.pack = pack;
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.textureName = textureName;
        this.shapeName = shapeName;
        setUnlocalizedName(pack.getName() + "_" + identifier);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
        if (alwaysEdible) {
            setAlwaysEdible();
        }

        Almura.PROXY.onNewItemInstance(this);
    }

    public static PackFood createFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild("Title").getString(name);
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final boolean showInCreativeTab = reader.getChild("show-in-creative-tab").getBoolean(true);
        final String creativeTabName = reader.getChild("creative-tab-name").getString("other");

        final int healAmount = reader.getChild("heal-amount").getInt(1);
        final float saturationModifier = reader.getChild("saturation-modifier").getFloat(1);
        final boolean isWolfFavorite = reader.getChild("is-wolf-favorite").getBoolean(true);
        final boolean alwaysEdible = reader.getChild("always-edible").getBoolean(false);

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(reader);

        Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "_" + name + ".name", title);

        return new PackFood(pack, name, textureName, shapeName, textureCoordinatesByFace, showInCreativeTab, creativeTabName, healAmount,
                            saturationModifier, isWolfFavorite, alwaysEdible);
    }

    @Override
    public void registerIcons(IIconRegister register) {
        itemIcon = new PackIcon(pack.getName(), textureName).register((TextureMap) register);

        if (textureCoordinatesByFace.isEmpty()) {
            return;
        }

        clippedIcons = PackUtil.generateClippedIconsFromCoords(pack, itemIcon, textureName, textureCoordinatesByFace);
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return new ClippedIcon[0];
    }

    public ContentPack getPack() {
        return pack;
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
        return "PackFood {pack= " + pack.getName() + ", raw_name= " + getUnlocalizedName() + "}";
    }
}
