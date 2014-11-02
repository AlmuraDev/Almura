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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a block created from a {@link YamlConfiguration}.
 */
public class YamlBlock extends Block {
    public static int renderId;
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    private final String shapeName;
    public ClippedIcon[] textureIcons;
    private SMPShape shape;

    public YamlBlock(String packName, String identifier) {
        this(packName, identifier, identifier, 1f, 0f, 0, null, null);
    }

    public YamlBlock(String packName, String identifier, String textureName) {
        this(packName, identifier, textureName, 1f, 0f, 0, null, null);
    }

    public YamlBlock(String packName, String identifier, String textureName, float hardness) {
        this(packName, identifier, textureName, hardness, 0f, 0, null, null);
    }

    public YamlBlock(String packName, String identifier, String textureName, float hardness, float lightLevel) {
        this(packName, identifier, textureName, hardness, lightLevel, 0, null, null);
    }

    public YamlBlock(String packName, String identifier, String textureName, float hardness, float lightLevel, int lightOpacity,
                     Map<Integer, List<Integer>> textureCoordinatesByFace, String shapeName) {
        super(Material.rock);
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.shapeName = shapeName;
        setBlockName(identifier);
        setBlockTextureName(Almura.MOD_ID + ":smps/" + textureName);
        setHardness(hardness);
        setLightLevel(lightLevel);
        setLightOpacity(lightOpacity);
        setCreativeTab(Tabs.LEGACY);
        GameRegistry.registerBlock(this, BasicItemBlock.class, packName + "." + identifier);
    }

    public static YamlBlock createFromSMPFile(String packName, Path file) throws FileNotFoundException, ConfigurationException {
        if (!file.endsWith(".yml")) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.warn("Attempted to load a block from file that was not YAML: " + file);
            }
            return null;
        }

        final String fileName = file.toFile().getName().split(".yml")[0];
        return createFromSMPStream(packName, fileName, new FileInputStream(file.toFile()));
    }

    public static YamlBlock createFromSMPStream(String packName, String name, InputStream stream) throws ConfigurationException {
        final YamlConfiguration reader = new YamlConfiguration(stream);
        reader.load();

        final String title = reader.getChild("Title").getString(name);
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final float hardness = reader.getChild("Hardness").getFloat(1f);
        final float lightLevel = reader.getChild("LightLevel").getFloat(0f);

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

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, "tile." + packName + "." + name + ".name", title);

        return new YamlBlock(packName, name, textureName, hardness, lightLevel, 0, textureCoordinatesByFace, shapeName);
    }

    @SideOnly(Side.CLIENT)
    public SMPShape getShape() {
        return shape;
    }

    @SideOnly(Side.CLIENT)
    public void applyShapeFromPack(SMPPack pack) {
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

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        if (textureCoordinatesByFace.isEmpty()) {
            super.registerBlockIcons(register);
            return;
        }

        blockIcon = new MalisisIcon(getTextureName()).register((TextureMap) register);

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

        textureIcons = new ClippedIcon[textureCoordinatesByFace.size()];

        for (int i = 0; i < textureCoordinatesByFace.size(); i++) {
            final List<Integer> coordList = textureCoordinatesByFace.get(i);

            textureIcons[i] =
                    new ClippedIcon((MalisisIcon) blockIcon, (float) (coordList.get(0) / dimension.getWidth()),
                                    (float) (coordList.get(1) / dimension.getHeight()), (float) (coordList.get(2) / dimension.getWidth()),
                                    (float) (coordList.get(3) / dimension.getHeight()));
        }
    }

    @Override
    public IIcon getIcon(int side, int type) {
        if (textureIcons == null) {
            return super.getIcon(side, type);
        }

        ClippedIcon sideIcon;

        if (side >= textureIcons.length) {
            sideIcon = textureIcons[0];
        } else {
            sideIcon = textureIcons[side];

            if (sideIcon == null) {
                sideIcon = textureIcons[0];
            }
        }

        return sideIcon;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return shape == null;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return shape == null;
    }

    @Override
    public String toString() {
        return "YamlBlock {raw_name= " + getUnlocalizedName() + "}";
    }
}
