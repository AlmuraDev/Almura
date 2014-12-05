/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.crop.stage.Stage;
import com.almuradev.almura.pack.crop.stage.property.HydrationProperty;
import com.almuradev.almura.pack.crop.stage.property.source.HydrationSource;
import com.almuradev.almura.pack.item.PackFood;
import com.almuradev.almura.pack.item.PackItem;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PackCreator {

    public static PackBlock createBlockFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild("Title").getString(name).split("\\n")[0];
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final float hardness = reader.getChild("Hardness").getFloat(1f);
        float lightLevel = reader.getChild("LightLevel").getFloat(0f);
        if (lightLevel > 1f) {
            lightLevel = lightLevel / 15;
        }
        final int lightOpacity = reader.getChild("Light-Opacity").getInt(0);
        final int dropAmount = reader.getChild("ItemDropAmount").getInt(0);
        final boolean showInCreativeTab = reader.getChild("Show-In-Creative-Tab").getBoolean(true);
        final String creativeTabName = reader.getChild("Creative-Tab-Name").getString("other");
        final float resistance = reader.getChild("Resistance").getFloat(0);
        final boolean rotation = reader.getChild("Rotation").getBoolean(true);
        final boolean mirrorRotation = reader.getChild("MirrorRotate").getBoolean(false);
        final boolean renderAsNormalBlock = reader.getChild("Render-As-Normal-Block").getBoolean(true);
        final boolean renderAsOpaque = reader.getChild("Render-As-Opaque").getBoolean(false);
        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(reader.getChild("Coords"));

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "\\" + name + ".name", title);

        return new PackBlock(pack, name, textureName, hardness, dropAmount, resistance, rotation, mirrorRotation, lightLevel, lightOpacity,
                             showInCreativeTab, creativeTabName, textureCoordinatesByFace, shapeName, renderAsNormalBlock, renderAsOpaque);
    }

    public static PackItem createItemFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String combinedTitleTooltips = reader.getChild("Title").getString(name);
        final String[] titleLines = combinedTitleTooltips.split("\\n");
        final String title = titleLines[0];
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final boolean showInCreativeTab = reader.getChild("Show-In-Creative-tab").getBoolean(true);
        final String creativeTabName = reader.getChild("Creative-Tab-Name").getString("other");

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(reader.getChild("Coords"));

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + name + ".name", title);

        return new PackItem(pack, name, titleLines.length == 1 ? null : Arrays.copyOfRange(titleLines, 1, titleLines.length), textureName, shapeName,
                            textureCoordinatesByFace, showInCreativeTab, creativeTabName);
    }

    public static PackFood createFoodFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
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

        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(reader.getChild("Coords"));

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + name + ".name", title);

        return new PackFood(pack, name, titleLines.length == 1 ? null : Arrays.copyOfRange(titleLines, 1, titleLines.length), textureName, shapeName,
                            textureCoordinatesByFace, showInCreativeTab, creativeTabName, healAmount,
                            saturationModifier, isWolfFavorite, alwaysEdible);
    }

    public static Stage createStageFromNode(int id, PackCrops crop, ConfigurationNode node) {
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(node.getChild("Coords"));
        String shapeName = node.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }
        final ConfigurationNode hydrationNode = node.getNode("Hydration");
        final boolean hydrationEnabled = hydrationNode.getChild("Enabled").getBoolean(false);
        final ConfigurationNode hydrationSourcesNode = hydrationNode.getNode("Sources");
        final List<HydrationSource> hydrationSources = Lists.newArrayList();
        final HydrationProperty hydrationProperty;

        if (hydrationSourcesNode != null) {
            for (Map.Entry<String, ConfigurationNode> entry : hydrationSourcesNode.getChildren().entrySet()) {
                final String[] split = entry.getKey().split(StringEscapeUtils.escapeJava("\\"));
                if (split.length == 1) {
                    Almura.LOGGER.warn("An invalid hydration source was provided [" + entry.getKey() + "].");
                    continue;
                }
                String identifier = split[1];
                if (split.length > 2) {
                    for (int i = 1; i < split.length; ++i) {
                        identifier = identifier + "\\" + split[i];
                    }
                }
                final Block block = GameRegistry.findBlock(split[0], identifier);
                if (block == null) {
                    Almura.LOGGER.warn("Could not find block [" + identifier + "] provided by mod [" + split[0] + "] for stage [" + id + "] in crop [" + crop + "].");
                    continue;
                }
                final int neededProximity = entry.getValue().getChild("Needed-Proximity").getInt(6);
                hydrationSources.add(new HydrationSource(block, neededProximity));
            }
            hydrationProperty = new HydrationProperty(hydrationEnabled, hydrationSources.toArray(new HydrationSource[hydrationSources.size()]));
        } else {
            hydrationProperty = new HydrationProperty(false, null);
        }

        return null;
    }
}
