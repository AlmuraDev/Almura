/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.blocks.yaml;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.items.BasicItemBlock;
import com.almuradev.almura.lang.Languages;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.registry.GameRegistry;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a block created from a {@link YamlConfiguration}.
 */
public class YamlBlock extends Block {
    private final String shapeName;
    private final Map<ForgeDirection, List<Integer>> coordsByFace;

    public YamlBlock(String identifier) {
        this(identifier, identifier, 1f, 1f, 0, null, null);
    }

    public YamlBlock(String identifier, String textureName) {
        this(identifier, textureName, 1f, 1f, 0, null, null);
    }

    public YamlBlock(String identifier, String textureName, float hardness) {
        this(identifier, textureName, hardness, 1f, 0, null, null);
    }

    public YamlBlock(String identifier, String textureName, float hardness, float lightLevel) {
        this(identifier, textureName, hardness, lightLevel, 0, null, null);
    }

    public YamlBlock(String identifier, String textureName, float hardness, float lightLevel, int lightOpacity, String shapeName, Map<ForgeDirection, List<Integer>> coordsByFace) {
        super(Material.rock);
        setBlockName(identifier);
        setBlockTextureName(Almura.MOD_ID + ":smps/" + textureName);
        setHardness(hardness);
        setLightLevel(lightLevel);
        setLightOpacity(lightOpacity);
        setCreativeTab(Tabs.LEGACY);
        this.shapeName = shapeName;
        this.coordsByFace = coordsByFace;
        GameRegistry.registerBlock(this, BasicItemBlock.class, identifier);
    }

    public static YamlBlock createFromSMPFile(Path file) throws FileNotFoundException, ConfigurationException {
        if (!file.endsWith(".yml")) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.warn("Attempted to load a block from file that was not YAML: " + file);
            }
            return null;
        }

        final String fileName = file.toFile().getName().split(".yml")[0];
        return createFromSMPStream(fileName, new FileInputStream(file.toFile()));
    }

    public static YamlBlock createFromSMPStream(String name, InputStream stream) throws ConfigurationException {
        final YamlConfiguration reader = new YamlConfiguration(stream);
        reader.load();

        final String title = reader.getChild("Title").getString(name);
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final float hardness = reader.getChild("Hardness").getFloat(1f);

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }
        final List<String> coordList = reader.getChild("Coords").getStringList();

        Map<ForgeDirection, List<Integer>> coordsByFace = new HashMap<>();

        for (int i = 0; i < coordList.size(); i++) {
            final String[] coordSplit = coordList.get(i).split(" ");

            final List<Integer> coords = new LinkedList<>();
            for (String coord : coordSplit) {
                coords.add(Integer.parseInt(coord));
            }

            coordsByFace.put(ForgeDirection.getOrientation(i), coords);
        }

        Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, "tile." + name + ".name", title);

        return new YamlBlock(name, textureName, hardness, 1f, 0, shapeName, coordsByFace);
    }

    @Override
    public String toString() {
        return "YamlBlock {raw_name= " + getUnlocalizedName() + "}";
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        if (coordsByFace.isEmpty()) {
            super.registerBlockIcons(register);
            return;
        }

        blockIcon = new MalisisIcon(getTextureName()).register((TextureMap) register);
    }

    @Override
    public IIcon getIcon(int side, int type) {
        if (coordsByFace.isEmpty()) {
            return super.getIcon(side, type);
        }

        final List<Integer> coordList;
        if (coordsByFace.size() - 1 < side) {
            coordList = coordsByFace.get(ForgeDirection.getOrientation(0));
        } else {
            coordList = coordsByFace.get(ForgeDirection.getOrientation(side));
        }

        return ((MalisisIcon) blockIcon).copy().clip(coordList.get(0), coordList.get(1), coordList.get(2), coordList.get(3));
    }
}
