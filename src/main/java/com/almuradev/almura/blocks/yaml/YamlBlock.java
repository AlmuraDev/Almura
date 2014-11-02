/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.blocks.yaml;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.items.BasicItemBlock;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.resource.SMPShape;
import com.almuradev.almura.smp.SMPPack;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a block created from a {@link YamlConfiguration}.
 */
public class YamlBlock extends Block {
    public static int renderId;
    public ClippedIcon[] clippedIcons;
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    private final String shapeName;
    private SMPShape shape;

    public YamlBlock(String packName, String identifier) {
        this(packName, identifier, identifier, 1f, 0f, 0, true, "legacy", null, null);
    }

    public YamlBlock(String packName, String identifier, String textureName) {
        this(packName, identifier, textureName, 1f, 0f, 0, true, "legacy", null, null);
    }

    public YamlBlock(String packName, String identifier, String textureName, float hardness) {
        this(packName, identifier, textureName, hardness, 0f, 0, true, "legacy", null, null);
    }

    public YamlBlock(String packName, String identifier, String textureName, float hardness, float lightLevel) {
        this(packName, identifier, textureName, hardness, lightLevel, 0, true, "legacy", null, null);
    }

    public YamlBlock(String packName, String identifier, String textureName, float hardness, float lightLevel, int lightOpacity,
                     boolean showInCreativeTab, String creativeTabName, Map<Integer, List<Integer>> textureCoordinatesByFace, String shapeName) {
        super(Material.rock);
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.shapeName = shapeName;
        setBlockName(packName + "." + identifier);
        setBlockTextureName(Almura.MOD_ID + ":smps/" + textureName);
        setHardness(hardness);
        setLightLevel(lightLevel);
        setLightOpacity(lightOpacity);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
        GameRegistry.registerBlock(this, BasicItemBlock.class, packName + "." + identifier);
    }

    public static YamlBlock createFromFile(String packName, Path file) throws IOException, ConfigurationException {
        if (!file.endsWith(".yml")) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.warn("Attempted to load a block from file that was not YAML: " + file);
            }
            return null;
        }

        final String fileName = file.toFile().getName().split(".yml")[0];
        final FileInputStream stream = new FileInputStream(file.toFile());
        final YamlConfiguration reader = new YamlConfiguration(stream);
        reader.load();
        final YamlBlock block = createFromReader(packName, fileName, reader);
        stream.close();
        return block;
    }

    public static YamlBlock createFromReader(String packName, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild("Title").getString(name);
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final float hardness = reader.getChild("Hardness").getFloat(1f);
        final float lightLevel = reader.getChild("LightLevel").getFloat(0f);
        final int lightOpacity = reader.getChild("light-opacity").getInt(0);
        final boolean showInCreativeTab = reader.getChild("show-in-creative-tab").getBoolean(true);
        final String creativeTabName = reader.getChild("creative-tab-name").getString("legacy");

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = extractCoordsFrom(reader);

        Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, "tile." + packName + "." + name + ".name", title);

        return new YamlBlock(packName, name, textureName, hardness, lightLevel, lightOpacity, showInCreativeTab, creativeTabName, textureCoordinatesByFace, shapeName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        if (textureCoordinatesByFace.isEmpty()) {
            super.registerBlockIcons(register);
            return;
        }

        blockIcon = new MalisisIcon(getTextureName()).register((TextureMap) register);

        applyClippedIconsFromCoords(textureCoordinatesByFace);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        if (clippedIcons == null) {
            return super.getIcon(side, type);
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

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock() {
        return shape == null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isOpaqueCube()
    {
        return shape == null;
    }


    @SideOnly(Side.CLIENT)
    public SMPShape getShape() {
        return shape;
    }

    @SideOnly(Side.CLIENT)
    public void setShapeFromPack(SMPPack pack) {
        this.shape = null;

        if (shapeName != null) {
            for (SMPShape shape : pack.getShapes()) {
                if (shape.getName().equals(shapeName)) {
                    this.shape = shape;
                    break;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void applyClippedIconsFromCoords(Map<Integer, List<Integer>> texCoords) {
        Dimension dimension = null;

        try {
            final Path imgPath = Paths.get(Filesystem.ASSETS_TEXTURES_BLOCKS_SMPS_PATH.toString(), textureName.split("/")[1] + ".png");
            dimension = Filesystem.getImageDimension(imgPath);
        } catch (IOException e) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.error("Failed to load texture [" + textureName + "] for dimensions", e);
            }
        }

        if (dimension == null) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.error("Failed to calculate the dimensions for texture [" + textureName + "]");
            }
            return;
        }

        clippedIcons = new ClippedIcon[texCoords.size()];

        for (int i = 0; i < texCoords.size(); i++) {
            final List<Integer> coordList = texCoords.get(i);

            clippedIcons[i] =
                    new ClippedIcon((MalisisIcon) blockIcon, (float) (coordList.get(0) / dimension.getWidth()),
                                    (float) (coordList.get(1) / dimension.getHeight()), (float) (coordList.get(2) / dimension.getWidth()),
                                    (float) (coordList.get(3) / dimension.getHeight()));
        }
    }

    @SideOnly(Side.CLIENT)
    public static Map<Integer, List<Integer>> extractCoordsFrom(YamlConfiguration reader) {
        final List<String> textureCoordinatesList = reader.getChild("Coords").getStringList();

        final Map<Integer, List<Integer>> textureCoordinatesByFace = new HashMap<>();

        for (int i = 0; i < textureCoordinatesList.size(); i++) {
            final String[] coordSplit = textureCoordinatesList.get(i).split(" ");

            final List<Integer> coords = new LinkedList<>();
            for (String coord : coordSplit) {
                coords.add(Integer.parseInt(coord));
            }

            textureCoordinatesByFace.put(i, coords);
        }

        return textureCoordinatesByFace;
    }

    @Override
    public String toString() {
        return "YamlBlock {raw_name= " + getUnlocalizedName() + "}";
    }
}
