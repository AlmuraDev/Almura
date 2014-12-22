/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.GameObjectMapper;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.crop.PackSeeds;
import com.almuradev.almura.pack.crop.Stage;
import com.almuradev.almura.pack.item.PackFood;
import com.almuradev.almura.pack.item.PackItem;
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.CollisionNode;
import com.almuradev.almura.pack.node.ConsumptionNode;
import com.almuradev.almura.pack.node.DropsNode;
import com.almuradev.almura.pack.node.GrassNode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.node.RenderNode;
import com.almuradev.almura.pack.node.RotationNode;
import com.almuradev.almura.pack.node.ToolsNode;
import com.almuradev.almura.pack.node.property.BonusProperty;
import com.almuradev.almura.pack.node.property.CollisionProperty;
import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.node.property.RotationProperty;
import com.almuradev.almura.pack.node.property.VariableGameObjectProperty;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PackCreator {

    private static final char[] RECIPE_MATRIX_PLACEHOLDER = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};

    public static PackBlock createBlockFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];
        final String shapeName = reader.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinates = PackUtil.parseCoordinatesFrom(reader.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey()).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final boolean showInCreativeTab = reader.getChild(PackKeys.SHOW_IN_CREATIVE_TAB.getKey()).getBoolean(
                PackKeys.SHOW_IN_CREATIVE_TAB.getDefaultValue());
        final String creativeTabName = reader.getChild(PackKeys.CREATIVE_TAB.getKey()).getString(PackKeys.CREATIVE_TAB.getDefaultValue());

        final float hardness = reader.getChild(PackKeys.HARDNESS.getKey()).getFloat(PackKeys.HARDNESS.getDefaultValue());
        final float resistance = reader.getChild(PackKeys.RESISTANCE.getKey()).getFloat(PackKeys.RESISTANCE.getDefaultValue());

        final RotationNode rotationNode = createRotationNode(pack, name, reader.getNode(PackKeys.NODE_ROTATE.getKey()));
        final LightNode lightNode = createLightNode(pack, name, reader.getNode(PackKeys.NODE_LIGHT.getKey()));
        final RenderNode renderNode = createRenderNode(pack, name, reader.getNode(PackKeys.NODE_RENDER.getKey()));
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "\\" + name + ".name", title);

        return new PackBlock(pack, name, textureName, textureCoordinates, shapeName, hardness, resistance, showInCreativeTab, creativeTabName,
                             rotationNode, lightNode, renderNode);
    }

    public static PackItem createItemFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final List<String> tooltip = reader.getChild(PackKeys.TOOLTIP.getKey()).getStringList();
        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];
        final String shapeName = reader.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinates = PackUtil.parseCoordinatesFrom(reader.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey()).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final boolean showInCreativeTab = reader.getChild(PackKeys.SHOW_IN_CREATIVE_TAB.getKey()).getBoolean(
                PackKeys.SHOW_IN_CREATIVE_TAB.getDefaultValue());
        final String creativeTabName = reader.getChild(PackKeys.CREATIVE_TAB.getKey()).getString(PackKeys.CREATIVE_TAB.getDefaultValue());

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + name + ".name", title);

        return new PackItem(pack, name, tooltip, textureName, shapeName, textureCoordinates, showInCreativeTab, creativeTabName);
    }

    public static PackFood createFoodFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final List<String> tooltip = reader.getChild(PackKeys.TOOLTIP.getKey()).getStringList();
        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];
        final String shapeName = reader.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinates = PackUtil.parseCoordinatesFrom(reader.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey()).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final boolean showInCreativeTab = reader.getChild(PackKeys.SHOW_IN_CREATIVE_TAB.getKey()).getBoolean(
                PackKeys.SHOW_IN_CREATIVE_TAB.getDefaultValue());
        final String creativeTabName = reader.getChild(PackKeys.CREATIVE_TAB.getKey()).getString(PackKeys.CREATIVE_TAB.getDefaultValue());
        final ConsumptionNode consumptionNode = createConsumptionNode(pack, name, reader.getNode(PackKeys.NODE_CONSUMPTION.getKey()));

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + name + ".name", title);

        return new PackFood(pack, name, tooltip, textureName, shapeName, textureCoordinates, showInCreativeTab, creativeTabName, consumptionNode);
    }

    public static PackCrops createCropFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];

        final int levelRequired = reader.getChild(PackKeys.LEVEL_REQUIRED.getKey()).getInt(PackKeys.LEVEL_REQUIRED.getDefaultValue());

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "\\" + name + ".name", title);

        return new PackCrops(pack, name, textureName, levelRequired, new Stage[0]);
    }

    public static PackSeeds createCropSeed(Pack pack, Block soil, PackCrops crop, String textureName, ConfigurationNode node)
            throws ConfigurationException {
        final String identifier = crop.getIdentifier() + "\\seed";
        final String title = node.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final List<String> tooltip = node.getChild(PackKeys.TOOLTIP.getKey()).getStringList();
        final String shapeName = node.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinates = PackUtil.parseCoordinatesFrom(node.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey()).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final boolean showInCreativeTab = node.getChild(PackKeys.SHOW_IN_CREATIVE_TAB.getKey()).getBoolean(
                PackKeys.SHOW_IN_CREATIVE_TAB.getDefaultValue());
        final String creativeTabName = node.getChild(PackKeys.CREATIVE_TAB.getKey()).getString(PackKeys.CREATIVE_TAB.getDefaultValue());

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + identifier, title);
        final PackSeeds
                seed =
                new PackSeeds(pack, identifier, tooltip, textureName, shapeName, textureCoordinates, showInCreativeTab, creativeTabName, crop, soil);
        seed.addNode(createGrassNode(pack, seed, node.getNode(PackKeys.NODE_GRASS.getKey())));
        return seed;
    }

    @SuppressWarnings("unchecked")
    public static Stage createCropStage(Pack pack, PackCrops crop, int id, ConfigurationNode node) {
        final String shapeName = node.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.parseCoordinatesFrom(node.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey(), true).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final LightNode lightNode = createLightNode(pack, crop.getIdentifier(), node.getNode(PackKeys.NODE_LIGHT.getKey()));

        return new Stage(crop, id, textureCoordinatesByFace, shapeName, null);
    }

    public static void createRecipeNode(Pack pack, String name, Object result, ConfigurationNode node) {
        for (Map.Entry<String, ConfigurationNode> entry : node.getChildren().entrySet()) {
            int id;
            try {
                id = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                    Almura.LOGGER
                            .error("Recipe id [" + entry.getKey() + "] in [" + name + "] in pack [" + pack.getName() + "] is not a valid number.", e);
                } else {
                    Almura.LOGGER
                            .warn("Recipe id [" + entry.getKey() + "] in [" + name + "] in pack [" + pack.getName() + "] is not a valid number.");
                }
                continue;
            }
            final String type = entry.getValue().getChild(PackKeys.TYPE.getKey()).getString(PackKeys.TYPE.getDefaultValue()).toUpperCase();
            switch (type) {
                case "SHAPED":
                    addMinecraftRecipe(pack, name, result, id, true, entry.getValue());
                    break;
                case "SHAPELESS":
                    addMinecraftRecipe(pack, name, result, id, false, entry.getValue());
                    break;
                default:
                    Almura.LOGGER.error("Type [" + type + "] for recipe id [" + id + "] in [" + name + "] in pack [" + pack.getName()
                                        + "] is not valid. Valid types are [SHAPED, SHAPELESS].");
            }
        }
    }

    private static RotationNode createRotationNode(Pack pack, String name, ConfigurationNode root) {
        final boolean rotationEnabled = root.getChild(PackKeys.ENABLED.getKey()).getBoolean(true);
        final boolean defaultRotateEnabled = root.getChild(PackKeys.DEFAULT_ROTATE.getKey()).getBoolean(PackKeys.DEFAULT_ROTATE.getDefaultValue());
        final boolean defaultMirrorRotateEnabled = root.getChild(PackKeys.DEFAULT_MIRROR_ROTATE.getKey()).getBoolean(
                PackKeys.DEFAULT_MIRROR_ROTATE.getDefaultValue());
        final EnumMap<RotationMeta.Rotation, RotationProperty> rotationProperties = Maps.newEnumMap(RotationMeta.Rotation.class);

        final ConfigurationNode directionRotationNode = root.getNode(PackKeys.DIRECTION.getKey());
        for (String rawRotation : directionRotationNode.getKeys(false)) {
            final RotationMeta.Rotation rotation = RotationMeta.Rotation.getState(rawRotation);
            if (rotation == null) {
                Almura.LOGGER.warn("Rotation [" + rawRotation + "] in [" + name + "] in pack [" + pack.getName() + "] is not valid.");
                continue;
            }
            final ConfigurationNode specificDirectionRotationNode = directionRotationNode.getNode(rawRotation);
            final boolean
                    specificDirectionRotationEnabled =
                    specificDirectionRotationNode.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
            final float angle = specificDirectionRotationNode.getChild(PackKeys.ANGLE.getKey()).getFloat(PackKeys.ANGLE.getDefaultValue());
            final RotationMeta.Direction
                    directionX =
                    RotationMeta.Direction.getState(
                            specificDirectionRotationNode.getChild(PackKeys.DIRECTION_X.getKey()).getString(PackKeys.DIRECTION_X.getDefaultValue()));
            final RotationMeta.Direction
                    directionY =
                    RotationMeta.Direction.getState(
                            specificDirectionRotationNode.getChild(PackKeys.DIRECTION_Y.getKey()).getString(PackKeys.DIRECTION_Y.getDefaultValue()));
            final RotationMeta.Direction
                    directionZ =
                    RotationMeta.Direction.getState(
                            specificDirectionRotationNode.getChild(PackKeys.DIRECTION_Z.getKey()).getString(PackKeys.DIRECTION_Z.getDefaultValue()));
            final RotationProperty
                    rotationProperty =
                    new RotationProperty(specificDirectionRotationEnabled, rotation, angle, directionX, directionY, directionZ);
            rotationProperties.put(rotation, rotationProperty);
        }

        return new RotationNode(rotationEnabled, defaultRotateEnabled, defaultMirrorRotateEnabled, rotationProperties);
    }

    private static LightNode createLightNode(Pack pack, String name, ConfigurationNode root) {
        float emission = root.getChild(PackKeys.EMISSION.getKey()).getFloat(PackKeys.EMISSION.getDefaultValue());
        if (emission < 0f) {
            emission = 0f;
        }
        if (emission > 1f) {
            emission = emission / 15f;
        }
        int opacity = root.getChild(PackKeys.OPACITY.getKey()).getInt(PackKeys.OPACITY.getDefaultValue());
        if (opacity < 0) {
            opacity = 0;
        }
        if (opacity > 255) {
            opacity = 255;
        }
        final ConfigurationNode lightRequiredConfigurationNode = root.getNode(PackKeys.REQUIRED.getKey());
        final boolean enabled = lightRequiredConfigurationNode.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
        final int min = lightRequiredConfigurationNode.getChild(PackKeys.MIN.getKey()).getInt(PackKeys.MIN.getDefaultValue());
        final int max = lightRequiredConfigurationNode.getChild(PackKeys.MAX.getKey()).getInt(PackKeys.MAX.getDefaultValue());
        return new LightNode(emission, opacity, new RangeProperty<>(Integer.class, enabled, min, max));
    }

    @SuppressWarnings("unchecked")
    private static CollisionNode createCollisionNode(Pack pack, String name, ConfigurationNode root) {
        final boolean collisionEnabled = root.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
        final ConfigurationNode collisionSourcesConfigurationNode = root.getNode(PackKeys.SOURCES.getKey());
        final Set<CollisionProperty> collisionProperties = Sets.newHashSet();
        if (collisionSourcesConfigurationNode != null) {
            for (String rawCollisionSource : collisionSourcesConfigurationNode.getKeys(false)) {
                final String[] entityIdentifierSplit = rawCollisionSource.split(StringEscapeUtils.escapeJava("\\"));
                String entityIdentifier = entityIdentifierSplit[0];
                if (entityIdentifierSplit.length > 1) {
                    for (int i = 1; i < entityIdentifierSplit.length; i++) {
                        entityIdentifier = entityIdentifier + "." + entityIdentifierSplit[i];
                    }
                }
                final Class<? extends Entity> entityClazz = (Class<? extends Entity>) EntityList.stringToClassMapping.get(entityIdentifier);
                if (entityClazz == null) {
                    Almura.LOGGER.warn("Entity source [" + rawCollisionSource + "] in [" + name + "] for modid [" + entityIdentifierSplit[0]
                                       + "] in pack [" + pack.getName() + "] is not a registered Entity.");
                    continue;
                }
                final ConfigurationNode collisionSourceConfigurationNode = root.getChild(rawCollisionSource);
                final boolean
                        enabled =
                        collisionSourceConfigurationNode.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
                final float
                        healthChange =
                        collisionSourceConfigurationNode.getChild(PackKeys.HEALTH_CHANGE.getKey()).getFloat(PackKeys.HEALTH_CHANGE.getDefaultValue());
                collisionProperties.add(new CollisionProperty(enabled, entityClazz, healthChange));
            }
        }
        return new CollisionNode(collisionEnabled, collisionProperties);
    }

    private static RenderNode createRenderNode(Pack pack, String name, ConfigurationNode root) {
        final boolean renderAsNormalBlock = root.getChild(PackKeys.NORMAL_CUBE.getKey()).getBoolean(PackKeys.NORMAL_CUBE.getDefaultValue());
        final boolean renderAsOpaque = root.getChild(PackKeys.OPAQUE.getKey()).getBoolean(PackKeys.OPAQUE.getDefaultValue());
        return new RenderNode(renderAsNormalBlock, renderAsOpaque);
    }

    private static BreakNode createBreakNode(Pack pack, String name, ConfigurationNode root) {
        final boolean breakEnabled = root.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
        final ConfigurationNode toolsConfigurationNode = root.getNode(PackKeys.TOOLS.getKey());
        final Set<ToolsNode> tools = Sets.newHashSet();

        for (String rawToolSource : toolsConfigurationNode.getKeys(false)) {
            final Pair<String, String> toolModidIdentifierParsed = parseModidIdentifierFrom(rawToolSource);
            boolean validTool = false;
            if (rawToolSource.equalsIgnoreCase(toolModidIdentifierParsed.getValue())) {
                validTool = true;
            }
            Object tool = null;
            if (!validTool) {
                tool = getGameObject(toolModidIdentifierParsed.getKey(), toolModidIdentifierParsed.getValue());
                validTool = tool != null;
            }
            if (!validTool) {
                Almura.LOGGER.warn("Tool source [" + rawToolSource + "] in [" + name + "] for modid [" + toolModidIdentifierParsed.getKey()
                                   + "] + in pack [" + pack + "]  is not a registered Block or Item.");
                continue;
            }
            final ConfigurationNode toolConfigurationNode = toolsConfigurationNode.getNode(rawToolSource);
            final RangeProperty<Integer>
                    experienceRange =
                    new RangeProperty<>(Integer.class, true, getRange(Integer.class, toolsConfigurationNode.getChild(PackKeys.EXPERIENCE.getKey())
                            .getString(PackKeys.EXPERIENCE.getDefaultValue()), 0));
            final RangeProperty<Float>
                    exhaustionRange =
                    new RangeProperty<>(Float.class, true, getRange(Float.class, toolsConfigurationNode.getChild(PackKeys.EXHAUSTION_CHANGE.getKey())
                            .getString(PackKeys.EXHAUSTION_CHANGE.getDefaultValue()), 0.025F));
            final ConfigurationNode dropsConfigurationNode = toolConfigurationNode.getNode(PackKeys.DROPS.getDefaultValue());
            final Set<DropProperty> drops = Sets.newHashSet();

            for (String rawDropSource : dropsConfigurationNode.getKeys(false)) {
                final Pair<String, String> dropModidIdentifierParsed = parseModidIdentifierFrom(rawDropSource);
                final Object drop = getGameObject(dropModidIdentifierParsed.getKey(), dropModidIdentifierParsed.getValue());
                if (drop == null) {
                    Almura.LOGGER.warn("Drop source [" + rawDropSource + "] in [" + name + "] for modid [" + dropModidIdentifierParsed.getKey()
                                       + "] in pack [" + pack + "] is not a registered Block or Item!");
                    continue;
                }
                final ConfigurationNode
                        dropConfigurationNode =
                        dropsConfigurationNode.getNode(dropModidIdentifierParsed.getKey(), dropModidIdentifierParsed.getValue());
                final RangeProperty<Integer>
                        amountRange =
                        new RangeProperty<>(Integer.class, true, getRange(Integer.class, dropConfigurationNode.getChild(PackKeys.AMOUNT.getKey())
                                .getString(PackKeys.AMOUNT.getDefaultValue()), 1));
                final int data = dropsConfigurationNode.getChild(PackKeys.DATA.getKey()).getInt(PackKeys.DATA.getDefaultValue());
                final ConfigurationNode bonusConfigurationNode = dropConfigurationNode.getNode(PackKeys.BONUS.getKey());
                final boolean
                        bonusEnabled =
                        bonusConfigurationNode.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
                final RangeProperty<Integer>
                        bonusAmountRange =
                        new RangeProperty<>(Integer.class, true, getRange(Integer.class, bonusConfigurationNode.getChild(PackKeys.AMOUNT.getKey())
                                .getString(PackKeys.AMOUNT.getDefaultValue()), 1));
                final RangeProperty<Double>
                        bonusChanceRange =
                        new RangeProperty<>(Double.class, true, getRange(Double.class, bonusConfigurationNode.getChild(PackKeys.CHANCE.getKey())
                                .getString(PackKeys.CHANCE.getDefaultValue()), 100.0));
                drops.add(new DropProperty(drop, amountRange, data,
                                           new BonusProperty<>(Integer.class, bonusEnabled, bonusAmountRange, bonusChanceRange)));
            }

            tools.add(tool == null ? new ToolsNode.OffHand(experienceRange, exhaustionRange, new DropsNode(drops))
                                   : new ToolsNode(tool, experienceRange, exhaustionRange, new DropsNode(drops)));
        }
        return new BreakNode(breakEnabled, tools);
    }

    private static ConsumptionNode createConsumptionNode(Pack pack, String name, ConfigurationNode root) {
        final float healthChange = root.getChild(PackKeys.HEALTH_CHANGE.getKey()).getFloat(PackKeys.HEALTH_CHANGE.getDefaultValue());
        final float saturationChange = root.getChild(PackKeys.SATURATION_CHANGE.getKey()).getFloat(PackKeys.SATURATION_CHANGE.getDefaultValue());
        final boolean wolfFavorite = root.getChild(PackKeys.WOLF_FAVORITE.getKey()).getBoolean(PackKeys.WOLF_FAVORITE.getDefaultValue());
        final boolean alwaysEdible = root.getChild(PackKeys.ALWAYS_EDIBLE.getKey()).getBoolean(PackKeys.ALWAYS_EDIBLE.getDefaultValue());
        return new ConsumptionNode(true, healthChange, saturationChange, alwaysEdible, wolfFavorite);
    }

    private static GrassNode createGrassNode(Pack pack, PackSeeds seed, ConfigurationNode root) {
        final boolean enabled = root.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
        final String amountRaw = root.getChild(PackKeys.AMOUNT.getKey()).getString(PackKeys.AMOUNT.getDefaultValue());
        Pair<Integer, Integer> amountPair;
        try {
            amountPair = getRange(Integer.class, amountRaw, 1);
        } catch (NumberFormatException nfe) {
            Almura.LOGGER
                    .warn("Amount given for [" + seed.getIdentifier() + "] in pack [" + pack + "] is not valid. Should be in the format of 1-3.");
            amountPair = new ImmutablePair<>(1, 1);
        }
        final String chanceRaw = root.getChild(PackKeys.CHANCE.getKey()).getString(PackKeys.CHANCE.getDefaultValue());
        Pair<Double, Double> chancePair;
        try {
            chancePair = getRange(Double.class, chanceRaw, 100.0);
        } catch (NumberFormatException nfe) {
            Almura.LOGGER.warn("Chance given for [" + seed.getIdentifier() + "] in pack [" + pack
                               + "] is not valid. Should be in the format of 10.5-20.5.");
            chancePair = new ImmutablePair<>(100.0, 100.0);
        }

        return new GrassNode(enabled, new VariableGameObjectProperty(seed, new RangeProperty<>(Integer.class, true, amountPair), 0),
                             new RangeProperty<>(Double.class, true, chancePair));
    }

    private static void addMinecraftRecipe(Pack pack, String name, Object result, int id, boolean shaped, ConfigurationNode node) {
        final int amount = node.getChild(PackKeys.AMOUNT.getKey()).getInt(1);
        final int data = node.getChild(PackKeys.DATA.getKey()).getInt(PackKeys.DATA.getDefaultValue().intValue());
        final List<Object> params = Lists.newArrayList();

        for (String itemsRaw : node.getChild(PackKeys.INGREDIENTS.getKey()).getStringList()) {
            final String[] itemsSplit = itemsRaw.split(" ");
            for (String identifierCombined : itemsSplit) {
                final Object gameObject = getGameObject(identifierCombined);
                if (gameObject == null) {
                    Almura.LOGGER.warn("Recipe id [" + id + "] in [" + name + "] in pack [" + pack.getName()
                                       + "] cannot be registered. Ingredient [" + identifierCombined + "] was not found.");
                    return;
                } else {
                    params.add(gameObject);
                }
            }
        }

        if (!params.isEmpty()) {
            if (shaped) {
                int index = 0;
                final Map<Object, Character> objectViaParamMap = Maps.newHashMap();

                final List<Object> combinedParams = Lists.newArrayList();

                StringBuilder lineMatrixBuilder = new StringBuilder();
                for (Object param : params) {
                    if (param.getClass() != BlockAir.class) {
                        Character c = objectViaParamMap.get(param);
                        if (c == null) {
                            c = RECIPE_MATRIX_PLACEHOLDER[index];
                            objectViaParamMap.put(param, c);
                            index++;
                        }
                        lineMatrixBuilder.append(c);
                    } else {
                        lineMatrixBuilder.append(" ");
                    }
                    if (lineMatrixBuilder.length() == 3) {
                        combinedParams.add(lineMatrixBuilder.toString());
                        lineMatrixBuilder = new StringBuilder();
                    }
                }
                for (Map.Entry<Object, Character> entry : objectViaParamMap.entrySet()) {
                    combinedParams.add(entry.getValue());
                    combinedParams.add(entry.getKey());
                }
                if (result instanceof Item) {
                    GameRegistry.addShapedRecipe(new ItemStack((Item) result, amount, data), combinedParams.toArray());
                } else if (result instanceof Block) {
                    GameRegistry.addShapedRecipe(new ItemStack((Block) result, amount, data), combinedParams.toArray());
                }
            } else {
                final Iterator<Object> iter = params.iterator();
                while (iter.hasNext()) {
                    if (iter.next().getClass().equals(BlockAir.class)) {
                        iter.remove();
                    }
                }

                if (result instanceof Item) {
                    GameRegistry.addShapedRecipe(new ItemStack((Item) result, amount, data), params.toArray());
                } else if (result instanceof Block) {
                    GameRegistry.addShapedRecipe(new ItemStack((Block) result, amount, data), params.toArray());
                }
            }
        }
    }

    public static Object getGameObject(String rawSource) {
        final Pair<String, String> parsedModidIdentifier = parseModidIdentifierFrom(rawSource);
        return getGameObject(parsedModidIdentifier.getKey(), parsedModidIdentifier.getValue());
    }

    public static Object getGameObject(String modid, String identifier) {
        Object object = GameRegistry.findBlock(modid, identifier);
        if (object == null) {
            object = GameRegistry.findItem(modid, identifier);
            if (object == null) {
                final Optional<GameObjectMapper.TrioWrapper<Object, String, Object>> wrapper = GameObjectMapper.get(modid, identifier);
                if (wrapper.isPresent()) {
                    object = wrapper.get().object;
                }
            }
        }
        return object;
    }

    public static Pair<String, String> parseModidIdentifierFrom(String rawSource) {
        final String[] separated = rawSource.split(StringEscapeUtils.escapeJava("\\"));
        String modid = separated[0].toLowerCase();
        String identifier;
        if (separated.length > 1) {
            identifier = rawSource.split(modid + StringEscapeUtils.escapeJava("\\"))[1];
        } else {
            identifier = modid;
        }
        if (identifier.equalsIgnoreCase(modid)) {
            identifier = identifier.toLowerCase();
            modid = "minecraft";
        }

        return new ImmutablePair<>(modid, identifier);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number> Pair<N, N> getRange(Class<N> clazz, String rawRangeSource, N minimum) throws NumberFormatException {
        N minAmount = minimum, maxAmount = minAmount;
        if (!rawRangeSource.isEmpty()) {
            final String[] split = rawRangeSource.split("-");
            if (clazz == Integer.class) {
                minAmount = (N) new Integer(Integer.parseInt(split[0]));
                if (split.length > 1) {
                    maxAmount = (N) new Integer(Integer.parseInt(split[1]));
                } else {
                    maxAmount = minAmount;
                }
            } else if (clazz == Double.class) {
                minAmount = (N) new Double(Double.parseDouble(split[0]));
                if (split.length > 1) {
                    maxAmount = (N) new Double(Double.parseDouble(split[1]));
                } else {
                    maxAmount = minAmount;
                }
            } else if (clazz == Float.class) {
                minAmount = (N) new Float(Float.parseFloat(split[0]));
                if (split.length > 1) {
                    maxAmount = (N) new Float(Float.parseFloat(split[0]));
                } else {
                    maxAmount = minAmount;
                }
            }
        }
        return new ImmutablePair<>(minAmount, maxAmount);
    }
}
