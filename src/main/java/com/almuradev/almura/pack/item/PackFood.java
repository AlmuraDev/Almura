/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.item;

import com.almuradev.almura.Tabs;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
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
    private String[] tooltip;

    public PackFood(ContentPack pack, String identifier, String[] tooltip, String textureName, String shapeName,
                    Map<Integer, List<Integer>> textureCoordinatesByFace,
                    boolean showInCreativeTab, String creativeTabName, int healAmount, float saturationModifier, boolean isWolfFavorite,
                    boolean alwaysEdible) {
        super(healAmount, saturationModifier, isWolfFavorite);
        this.pack = pack;
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.tooltip = tooltip;
        setUnlocalizedName(pack.getName() + "_" + identifier);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
        if (alwaysEdible) {
            setAlwaysEdible();
        }
    }

    public static PackFood createFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String combinedTitleTooltips = reader.getChild("Title").getString(name);
        final String[] titleLines = combinedTitleTooltips.split("\\n");
        final String title = titleLines[0];
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final boolean showInCreativeTab = reader.getChild("Show-In-Creative-Tab").getBoolean(true);
        final String creativeTabName = reader.getChild("Creative-Tab-Name").getString("other");

        final int healAmount = reader.getChild("Heal-Amount").getInt(1);
        final float saturationModifier = reader.getChild("Saturation-Modifier").getFloat(1);
        final boolean isWolfFavorite = reader.getChild("Is-Wolf-Favorite").getBoolean(true);
        final boolean alwaysEdible = reader.getChild("Always-Edible").getBoolean(false);

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(reader);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "_" + name + ".name", title);

        return new PackFood(pack, name, titleLines.length == 1 ? null : Arrays.copyOfRange(titleLines, 1, titleLines.length), textureName, shapeName,
                            textureCoordinatesByFace, showInCreativeTab, creativeTabName, healAmount,
                            saturationModifier, isWolfFavorite, alwaysEdible);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean p_77624_4_) {
        if (tooltip != null && tooltip.length > 0) {
            Collections.addAll(list, tooltip);
        }
    }

    @Override
    public void registerIcons(IIconRegister register) {
        itemIcon = new PackIcon(pack.getName(), textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoords(pack, itemIcon, textureName, textureCoordinatesByFace);
    }

    @Override
    public ContentPack getPack() {
        return pack;
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
    public void setShapeFromPack() {
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
