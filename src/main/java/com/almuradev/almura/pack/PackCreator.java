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
import com.almuradev.almura.pack.property.CollisionProperty;
import com.almuradev.almura.pack.property.ConsumptionProperty;
import com.almuradev.almura.pack.property.HydrationProperty;
import com.almuradev.almura.pack.property.LightProperty;
import com.almuradev.almura.pack.property.RenderProperty;
import com.almuradev.almura.pack.property.source.CollisionSource;
import com.almuradev.almura.pack.property.source.HydrationSource;
import com.almuradev.almura.pack.property.source.RangeSource;
import com.almuradev.almura.pack.item.PackFood;
import com.almuradev.almura.pack.item.PackItem;
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PackCreator {
    private static final char[] RECIPE_MATRIX_PLACEHOLDER = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};

    public static PackBlock createBlockFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild(PackKeys.TITLE.getKey()).getString(PackKeys.TITLE.getDefaultValue());
        final String textureName = reader.getChild(PackKeys.TEXTURE.getKey()).getString(PackKeys.TEXTURE.getDefaultValue()).split(".png")[0];
        final String shapeName = reader.getChild(PackKeys.SHAPE.getKey()).getString(PackKeys.SHAPE.getDefaultValue()).split(".shape")[0];
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.parseCoordinatesFrom(reader.getChild(
                PackKeys.TEXTURE_COORDINATES.getKey()).getStringList(PackKeys.TEXTURE_COORDINATES.getDefaultValue()));
        final boolean showInCreativeTab = reader.getChild(PackKeys.SHOW_IN_CREATIVE_TAB.getKey()).getBoolean(
                PackKeys.SHOW_IN_CREATIVE_TAB.getDefaultValue());
        final String creativeTabName = reader.getChild(PackKeys.CREATIVE_TAB.getKey()).getString(PackKeys.CREATIVE_TAB.getDefaultValue());

        final float hardness = reader.getChild(PackKeys.HARDNESS.getKey()).getFloat(PackKeys.HARDNESS.getDefaultValue());
        final float resistance = reader.getChild(PackKeys.RESISTANCE.getKey()).getFloat(PackKeys.RESISTANCE.getDefaultValue());
        final boolean rotation = reader.getChild(PackKeys.ROTATE.getKey()).getBoolean(PackKeys.ROTATE.getDefaultValue());
        final boolean mirrorRotation = reader.getChild(PackKeys.MIRROR_ROTATE.getKey()).getBoolean(PackKeys.MIRROR_ROTATE.getDefaultValue());

        final ConfigurationNode lightNode = reader.getNode(PackKeys.NODE_LIGHT.getKey());
        float emission = lightNode.getChild(PackKeys.EMISSION.getKey()).getFloat(PackKeys.EMISSION.getDefaultValue());
        if (emission < 0f) {
            emission = 0f;
        }
        if (emission > 1f) {
            emission = emission / 15f;
        }

        int lightOpacity = lightNode.getChild(PackKeys.OPACITY.getKey()).getInt(PackKeys.OPACITY.getDefaultValue());
        if (lightOpacity < 0) {
            lightOpacity = 0;
        }
        if (lightOpacity > 255) {
            lightOpacity = 255;
        }
        final LightProperty lightProperty = new LightProperty(emission, lightOpacity, new RangeSource(false, 0, 0));
        final ConfigurationNode renderNode = reader.getNode(PackKeys.NODE_RENDER.getKey());
        final boolean renderAsNormalBlock = renderNode.getChild(PackKeys.NORMAL_CUBE.getKey()).getBoolean(PackKeys.NORMAL_CUBE.getDefaultValue());
        final boolean renderAsOpaque = renderNode.getChild(PackKeys.OPAQUE.getKey()).getBoolean(PackKeys.OPAQUE.getDefaultValue());
        final RenderProperty renderProperty = new RenderProperty(renderAsNormalBlock, renderAsOpaque);
        final boolean hasRecipe = reader.hasChild(PackKeys.NODE_RECIPES.getKey());

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "\\" + name + ".name", title);

        final int dropAmount = reader.getChild("ItemDropAmount").getInt(0);

        return new PackBlock(pack, name, textureName, hardness, dropAmount, resistance, rotation, mirrorRotation, lightProperty, showInCreativeTab,
                             creativeTabName, textureCoordinatesByFace, shapeName, renderProperty, hasRecipe);
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
        final float heal = consumptionNode.getChild(PackKeys.HEAL.getKey()).getFloat(PackKeys.HEAL.getDefaultValue());
        final float saturation = consumptionNode.getChild(PackKeys.SATURATION.getKey()).getFloat(PackKeys.SATURATION.getDefaultValue());
        final boolean wolfFavorite = consumptionNode.getChild(PackKeys.WOLF_FAVORITE.getKey()).getBoolean(PackKeys.WOLF_FAVORITE.getDefaultValue());
        final boolean alwaysEdible = consumptionNode.getChild(PackKeys.ALWAYS_EDIBLE.getKey()).getBoolean(PackKeys.ALWAYS_EDIBLE.getDefaultValue());
        final ConsumptionProperty consumptionProperty = new ConsumptionProperty(true, heal, saturation, alwaysEdible, wolfFavorite);

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
        final List<HydrationSource> hydrationSources = Lists.newArrayList();
        if (hydrationSourcesNode != null) {
            for (Map.Entry<String, ConfigurationNode> entry : hydrationSourcesNode.getChildren().entrySet()) {
                final String[] split = entry.getKey().split(StringEscapeUtils.escapeJava("\\"));
                if (split.length == 1) {
                    Almura.LOGGER.warn("An invalid hydration source was provided [" + entry.getKey() + "] for.");
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
                hydrationSources.add(new HydrationSource(hydrationSource, neededProximity));
            }
        }
        final HydrationProperty
                hydrationProperty =
                new HydrationProperty(hydrationEnabled, hydrationSources.toArray(new HydrationSource[hydrationSources.size()]));

        //LIGHT
        final ConfigurationNode lightNode = node.getNode("light");
        final int lightLevel = lightNode.getChild("emission").getInt(0);
        final int lightOpacity = lightNode.getChild("opacity").getInt(0);
        final ConfigurationNode lightRequiredNode = lightNode.getNode("required");
        final boolean lightRequiredEnabled = lightRequiredNode.getBoolean(true);
        final int lightRequiredMin = lightRequiredNode.getChild("min").getInt(0);
        final int lightRequiredMax = lightRequiredNode.getChild("max").getInt(15);
        final LightProperty
                lightProperty =
                new LightProperty(lightLevel, lightOpacity, new RangeSource(lightRequiredEnabled, lightRequiredMin, lightRequiredMax));

        //COLLISION
        final ConfigurationNode collisionNode = node.getNode("collision");
        final boolean collisionEnabled = collisionNode.getChild("enabled").getBoolean(false);
        final ConfigurationNode collisionSourcesNode = collisionNode.getNode("sources");
        final List<CollisionSource> collisionSources = Lists.newArrayList();
        if (collisionSourcesNode != null) {
            for (Map.Entry<String, ConfigurationNode> collisionSource : collisionSourcesNode.getChildren().entrySet()) {
                final String[] entityIdentifierSplit = collisionSource.getKey().split(StringEscapeUtils.escapeJava("\\"));
                String entityIdentifier = entityIdentifierSplit[0];
                if (entityIdentifierSplit.length > 1) {
                    for (int i = 1; i < entityIdentifierSplit.length; i++) {
                        entityIdentifier = entityIdentifier + "." + entityIdentifierSplit[i];
                    }
                }
                final Class<? extends Entity> entityClazz = (Class<? extends Entity>) EntityList.stringToClassMapping.get(entityIdentifier);
                if (entityClazz == null) {
                    Almura.LOGGER.warn("Unknown collision source [" + collisionSource.getKey() + "] provided for stage [" + id + "] in crop [" + crop
                            .getIdentifier() + "] in pack [" + pack.getName() + "].");
                }
                final float healthChange = collisionSourcesNode.getChild(collisionSource.getKey()).getFloat(0f);
                collisionSources.add(new CollisionSource(entityClazz, healthChange));
            }
        }
        final CollisionProperty
                collisionProperty =
                new CollisionProperty(collisionEnabled, collisionSources.toArray(new CollisionSource[collisionSources.size()]));

        return new Stage(crop, id, textureCoordinatesByFace, shapeName, null);
    }

    public static void createRecipeFromNode(Pack pack, String name, boolean isItem, ConfigurationNode node) {
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
