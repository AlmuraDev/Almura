/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.crop.PackSeeds;
import com.almuradev.almura.pack.crop.Stage;
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.CollisionNode;
import com.almuradev.almura.pack.node.ConsumptionNode;
import com.almuradev.almura.pack.node.HydrationNode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.node.RenderNode;
import com.almuradev.almura.pack.node.RotationNode;
import com.almuradev.almura.pack.node.property.CollisionProperty;
import com.almuradev.almura.pack.node.property.HydrationProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.item.PackFood;
import com.almuradev.almura.pack.item.PackItem;
import com.almuradev.almura.pack.node.property.RotationProperty;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PackCreator {
    private static final char[] RECIPE_MATRIX_PLACEHOLDER = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};

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

        final RotationNode rotationNode = createNode(pack, name, reader.getNode(PackKeys.ROTATE.getKey()));

        final ConfigurationNode lightConfigurationNode = reader.getNode(PackKeys.NODE_LIGHT.getKey());
        float emission = lightConfigurationNode.getChild(PackKeys.EMISSION.getKey()).getFloat(PackKeys.EMISSION.getDefaultValue());
        if (emission < 0f) {
            emission = 0f;
        }
        if (emission > 1f) {
            emission = emission / 15f;
        }

        int lightOpacity = lightConfigurationNode.getChild(PackKeys.OPACITY.getKey()).getInt(PackKeys.OPACITY.getDefaultValue());
        if (lightOpacity < 0) {
            lightOpacity = 0;
        }
        if (lightOpacity > 255) {
            lightOpacity = 255;
        }
        final LightNode lightNode = new LightNode(emission, lightOpacity, new RangeProperty<>(Integer.class, false, 0, 0));

        final ConfigurationNode renderConfigurationNode = reader.getNode(PackKeys.NODE_RENDER.getKey());
        final boolean renderAsNormalBlock = renderConfigurationNode.getChild(PackKeys.NORMAL_CUBE.getKey()).getBoolean(PackKeys.NORMAL_CUBE.getDefaultValue());
        final boolean renderAsOpaque = renderConfigurationNode.getChild(PackKeys.OPAQUE.getKey()).getBoolean(PackKeys.OPAQUE.getDefaultValue());
        final RenderNode renderNode = new RenderNode(renderAsNormalBlock, renderAsOpaque);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "\\" + name + ".name", title);

        return new PackBlock(pack, name, textureName, textureCoordinates, shapeName, hardness, resistance, showInCreativeTab, creativeTabName, rotationNode, lightNode, renderNode);
    }

    public static PackItem createItemFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final List<String> tooltip = reader.getChild(PackKeys.TOOLTIP.getKey()).getStringList();
        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];
        final String shapeName = reader.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.parseCoordinatesFrom(reader.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey()).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final boolean showInCreativeTab = reader.getChild(PackKeys.SHOW_IN_CREATIVE_TAB.getKey()).getBoolean(
                PackKeys.SHOW_IN_CREATIVE_TAB.getDefaultValue());
        final String creativeTabName = reader.getChild(PackKeys.CREATIVE_TAB.getKey()).getString(PackKeys.CREATIVE_TAB.getDefaultValue());

        final boolean hasRecipe = reader.hasChild(PackKeys.NODE_RECIPES.getKey());

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + name + ".name", title);

        return new PackItem(pack, name, tooltip, textureName, shapeName, textureCoordinatesByFace, showInCreativeTab, creativeTabName, hasRecipe);
    }

    public static PackFood createFoodFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final List<String> tooltip = reader.getChild(PackKeys.TOOLTIP.getKey()).getStringList();
        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];
        final String shapeName = reader.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.parseCoordinatesFrom(reader.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey()).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final boolean showInCreativeTab = reader.getChild(PackKeys.SHOW_IN_CREATIVE_TAB.getKey()).getBoolean(
                PackKeys.SHOW_IN_CREATIVE_TAB.getDefaultValue());
        final String creativeTabName = reader.getChild(PackKeys.CREATIVE_TAB.getKey()).getString(PackKeys.CREATIVE_TAB.getDefaultValue());

        final ConfigurationNode consumptionNode = reader.getNode(PackKeys.NODE_CONSUMPTION.getKey());
        final float heal = consumptionNode.getChild(PackKeys.HEALTH_CHANGE.getKey()).getFloat(PackKeys.HEALTH_CHANGE.getDefaultValue());
        final float saturation = consumptionNode.getChild(PackKeys.SATURATION_CHANGE.getKey()).getFloat(PackKeys.SATURATION_CHANGE.getDefaultValue());
        final boolean wolfFavorite = consumptionNode.getChild(PackKeys.WOLF_FAVORITE.getKey()).getBoolean(PackKeys.WOLF_FAVORITE.getDefaultValue());
        final boolean alwaysEdible = consumptionNode.getChild(PackKeys.ALWAYS_EDIBLE.getKey()).getBoolean(PackKeys.ALWAYS_EDIBLE.getDefaultValue());
        final ConsumptionNode consumptionProperty = new ConsumptionNode(true, heal, saturation, alwaysEdible, wolfFavorite);

        final boolean hasRecipe = reader.hasChild(PackKeys.NODE_RECIPES.getKey());

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + name + ".name", title);

        return new PackFood(pack, name, tooltip, textureName, shapeName, textureCoordinatesByFace, showInCreativeTab, creativeTabName, consumptionProperty, hasRecipe);
    }

    public static PackCrops createCropFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];

        final int levelRequired = reader.getChild(PackKeys.LEVEL_REQUIRED.getKey()).getInt(PackKeys.LEVEL_REQUIRED.getDefaultValue());

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "\\" + name + ".name", title);

        return new PackCrops(pack, name, textureName, levelRequired, new Stage[0]);
    }

    public static PackSeeds createSeedFromNode(Pack pack, Block soil, PackCrops crop, String textureName, ConfigurationNode node)
            throws ConfigurationException {
        final String identifier = node.getChild(PackKeys.IDENTIFIER.getKey()).getString(PackKeys.IDENTIFIER.getDefaultValue());
        final String title = node.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final List<String> tooltip = node.getChild(PackKeys.TOOLTIP.getKey()).getStringList();
        final String shapeName = node.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.parseCoordinatesFrom(node.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey()).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final boolean showInCreativeTab = node.getChild(PackKeys.SHOW_IN_CREATIVE_TAB.getKey()).getBoolean(
                PackKeys.SHOW_IN_CREATIVE_TAB.getDefaultValue());
        final String creativeTabName = node.getChild(PackKeys.CREATIVE_TAB.getKey()).getString(PackKeys.CREATIVE_TAB.getDefaultValue());
        final boolean dropOnGrassBreak = node.getChild("drop-on-grass-break").getBoolean(true);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + crop.getIdentifier() + "\\" + identifier, title);

        return new PackSeeds(pack, pack.getName() + "\\" + crop.getIdentifier() + "\\" + identifier, textureName, showInCreativeTab, creativeTabName, crop, soil);
    }

    @SuppressWarnings("unchecked")
    public static Stage createStageFromNode(Pack pack, PackCrops crop, int id, ConfigurationNode node) {
        final String shapeName = node.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.parseCoordinatesFrom(node.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey(), true).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));

        //HYDRATION
        final ConfigurationNode hydrationNode = node.getNode("hydration");
        final boolean hydrationEnabled = hydrationNode.getChild("enabled").getBoolean(false);
        final ConfigurationNode hydrationSourcesNode = hydrationNode.getNode("sources");
        final List<HydrationProperty> hydrationSources = Lists.newArrayList();
        if (hydrationSourcesNode != null) {
            for (Map.Entry<String, ConfigurationNode> entry : hydrationSourcesNode.getChildren().entrySet()) {
                final String[] split = entry.getKey().split(StringEscapeUtils.escapeJava("\\"));
                if (split.length == 1) {
                    Almura.LOGGER.warn("An invalid hydration property was provided [" + entry.getKey() + "] for.");
                    continue;
                }
                final String identifier = parseIdentifier(split);
                final Block hydrationSource = getFromRegistry(split[0], identifier, Block.class);
                if (hydrationSource == null) {
                    Almura.LOGGER.warn("Could not find block [" + identifier + "] provided by mod [" + split[0] + "] for stage [" + id + "] in crop ["
                                       + crop.getIdentifier() + "] in pack [" + pack.getName() + "].");
                    continue;
                }
                final int neededProximity = entry.getValue().getChild("needed-proximity").getInt(6);
                hydrationSources.add(new HydrationProperty(hydrationSource, neededProximity));
            }
        }
        final HydrationNode
                hydrationProperty =
                new HydrationNode(hydrationEnabled, hydrationSources.toArray(new HydrationProperty[hydrationSources.size()]));

        final LightNode lightNode = createLightNode(pack, crop.getIdentifier(), node.getNode(PackKeys.NODE_LIGHT.getKey()));

        return new Stage(crop, id, textureCoordinatesByFace, shapeName, null);
    }

    public static void createRecipeNode(Pack pack, String name, boolean isItem, ConfigurationNode node) {
        for (Map.Entry<String, ConfigurationNode> entry : node.getChildren().entrySet()) {
            int id;
            try {
                id = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                    Almura.LOGGER.error("Illegal id [" + entry.getKey() + "] for recipe specified in [" + name + "] for pack [" + pack.getName() + "]", e);
                } else {
                    Almura.LOGGER.warn("Illegal id [" + entry.getKey() + "] for recipe specified in [" + name + "] for pack [" + pack.getName() + "]");
                }
                continue;
            }
            final String type = entry.getValue().getChild(PackKeys.TYPE.getKey()).getString(PackKeys.TYPE.getDefaultValue()).toUpperCase();
            switch(type) {
                case "SHAPED":
                    addMinecraftRecipe(pack, name, id, true, isItem, entry.getValue());
                    break;
                case "SHAPELESS":
                    addMinecraftRecipe(pack, name, id, false, isItem, entry.getValue());
                    break;
                default:
                    Almura.LOGGER.error("Illegal type [" + type + "] for recipe id [" + id + "] specified in [" + name + "] for pack [" + pack.getName() + "]. Valid types are [SHAPED, SHAPELESS].");
            }
        }
    }

    private static RotationNode createRotationNode(Pack pack, String name, ConfigurationNode root) {
        final boolean rotationEnabled = root.getChild(PackKeys.ENABLED.getKey()).getBoolean(true);
        final boolean defaultRotateEnabled = root.getChild(PackKeys.DEFAULT_ROTATE.getKey()).getBoolean(PackKeys.DEFAULT_ROTATE.getDefaultValue());
        final boolean defaultMirrorRotateEnabled = root.getChild(PackKeys.DEFAULT_MIRROR_ROTATE.getKey()).getBoolean(
                PackKeys.DEFAULT_MIRROR_ROTATE.getDefaultValue());
        final EnumMap<IRotatable.Rotation, RotationProperty> rotationProperties = Maps.newEnumMap(IRotatable.Rotation.class);

        final ConfigurationNode directionRotationNode = root.getNode(PackKeys.DIRECTION.getKey());
        for (String rawRotation: directionRotationNode.getKeys(false)) {
            final IRotatable.Rotation rotation = IRotatable.Rotation.getState(rawRotation);
            if (rotation == null) {
                Almura.LOGGER.warn("Invalid rotation [" + rawRotation + "] specified in [" + name + "] in pack [" + pack.getName() + "].");
                continue;
            }
            final ConfigurationNode specificDirectionRotationNode = directionRotationNode.getNode(rawRotation);
            final boolean specificDirectionRotationEnabled = specificDirectionRotationNode.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
            final float angle = specificDirectionRotationNode.getChild(PackKeys.ANGLE.getKey()).getFloat(PackKeys.ANGLE.getDefaultValue());
            final IRotatable.Direction directionX = IRotatable.Direction.getState(specificDirectionRotationNode.getChild(PackKeys.DIRECTION_X.getKey()).getString(PackKeys.DIRECTION_X.getDefaultValue()));
            final IRotatable.Direction directionY = IRotatable.Direction.getState(specificDirectionRotationNode.getChild(PackKeys.DIRECTION_Y.getKey()).getString(PackKeys.DIRECTION_Y.getDefaultValue()));
            final IRotatable.Direction directionZ = IRotatable.Direction.getState(specificDirectionRotationNode.getChild(PackKeys.DIRECTION_Z.getKey()).getString(PackKeys.DIRECTION_Z.getDefaultValue()));
            final RotationProperty rotationProperty = new RotationProperty(specificDirectionRotationEnabled, rotation, angle, directionX, directionY, directionZ);
            rotationProperties.put(rotation, rotationProperty);
        }

        return new RotationNode(rotationEnabled, defaultRotateEnabled, defaultMirrorRotateEnabled, rotationProperties);
    }

    private static LightNode createLightNode(Pack pack, String name, ConfigurationNode root) {
        final float emission = root.getChild(PackKeys.EMISSION.getKey()).getFloat(PackKeys.EMISSION.getDefaultValue());
        final int opacity = root.getChild(PackKeys.OPACITY.getKey()).getInt(PackKeys.OPACITY.getDefaultValue());
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
        final List<CollisionProperty> collisionProperties = Lists.newArrayList();
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
                    Almura.LOGGER.warn("invalid collision source [" + rawCollisionSource + "] specified in [" + name + "] in pack [" + pack.getName() + "].");
                    continue;
                }
                final ConfigurationNode collisionSourceConfigurationNode = root.getChild(rawCollisionSource);
                final boolean enabled = collisionSourceConfigurationNode.getChild(PackKeys.ENABLED.getKey()).getBoolean(PackKeys.ENABLED.getDefaultValue());
                final float healthChange = collisionSourceConfigurationNode.getChild(PackKeys.HEALTH_CHANGE.getKey()).getFloat(PackKeys.HEALTH_CHANGE.getDefaultValue());
                collisionProperties.add(new CollisionProperty(enabled, entityClazz, healthChange));
            }
        }
        return new CollisionNode(collisionEnabled, collisionProperties.toArray(new CollisionProperty[collisionProperties.size()]));
    }

    private static BreakNode createBreakNode(Pack pack, String name, ConfigurationNode root) {
        return null;
    }

    private static void addMinecraftRecipe(Pack pack, String name, int id, boolean shaped, boolean itemResult, ConfigurationNode node) {
        final int amount = node.getChild(PackKeys.AMOUNT.getKey()).getInt(PackKeys.AMOUNT.getDefaultValue());
        final int damage = node.getChild(PackKeys.INTEGER_DAMAGE.getKey()).getInt(PackKeys.INTEGER_DAMAGE.getDefaultValue());
        final List<Object> params = Lists.newArrayList();

        for (String itemsRaw : node.getChild(PackKeys.INGREDIENTS.getKey()).getStringList()) {
            final String[] itemsSplit = itemsRaw.split(" ");
            for (String identifierCombined : itemsSplit) {
                final String[] separated = identifierCombined.split(StringEscapeUtils.escapeJava("\\"));
                final String modid = separated[0].toLowerCase();
                String identifier;
                if (separated.length > 1) {
                    identifier = identifierCombined.split(modid + StringEscapeUtils.escapeJava("\\"))[1];
                } else {
                    identifier = modid;
                }
                boolean minecraft = false;
                //Air -> air
                if (identifier.equalsIgnoreCase(modid)) {
                    identifier = identifier.toLowerCase();
                    minecraft = true;
                }
                if (!minecraft) {
                    minecraft = modid.equalsIgnoreCase("Minecraft");
                };
                final Block block = GameRegistry.findBlock(minecraft ? "minecraft" : modid, identifier);
                if (block == null) {
                    final Item item = GameRegistry.findItem(minecraft ? "minecraft" : modid, identifier);
                    if (item == null) {
                        Almura.LOGGER.warn("Could not add recipe id [" + id + "] in [" + name + "] requested by pack [" + pack.getName() + "]. The ingredient [" + identifierCombined + "] was not found in the GameRegistry!");
                        return;
                    } else {
                        params.add(item);
                    }
                } else {
                    params.add(block);
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
                if (itemResult) {
                    GameRegistry.addShapedRecipe(new ItemStack(GameRegistry.findItem("almura", pack.getName() + "\\" + name), amount, damage), combinedParams.toArray());
                } else {
                    final Block block = GameRegistry.findBlock("almura", pack.getName() + "\\" + name);
                    GameRegistry.addShapedRecipe(new ItemStack(block, amount, damage), combinedParams.toArray());
                }
            } else {
                final Iterator<Object> iter = params.iterator();
                while (iter.hasNext()) {
                    if (iter.next().getClass().equals(BlockAir.class)) {
                        iter.remove();
                    }
                }

                if (itemResult) {
                    GameRegistry.addShapelessRecipe(new ItemStack(GameRegistry.findItem("almura", pack.getName() + "\\" + name), amount, damage), params.toArray());
                } else {
                    GameRegistry.addShapelessRecipe(new ItemStack(GameRegistry.findBlock("almura", pack.getName() + "\\" + name), amount, damage), params.toArray());
                }
            }
        }
    }

    private static String parseIdentifier(String[] split) {
        String identifier = split[1];
        if (split.length > 2) {
            for (int i = 1; i < split.length; ++i) {
                identifier = identifier + "\\" + split[i];
            }
        }
        return identifier;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getFromRegistry(String modid, String identifier, Class<T> type) {

        if (type == Block.class) {
            return (T) GameRegistry.findBlock(modid, identifier);
        } else if (type == Item.class) {
            return (T) GameRegistry.findItem(modid, identifier);
        }

        return null;
    }
}
