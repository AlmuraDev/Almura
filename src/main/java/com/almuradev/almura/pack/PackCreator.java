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
import com.almuradev.almura.pack.crop.stage.Stage;
import com.almuradev.almura.pack.crop.stage.property.CollisionProperty;
import com.almuradev.almura.pack.crop.stage.property.HydrationProperty;
import com.almuradev.almura.pack.crop.stage.property.LightProperty;
import com.almuradev.almura.pack.crop.stage.property.source.CollisionSource;
import com.almuradev.almura.pack.crop.stage.property.source.HydrationSource;
import com.almuradev.almura.pack.crop.stage.property.source.RangeSource;
import com.almuradev.almura.pack.item.PackFood;
import com.almuradev.almura.pack.item.PackItem;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.registry.GameRegistry;
import net.malisis.core.renderer.model.loader.ObjFileImporter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PackCreator {
    private static final char[] RECIPE_MATRIX_PLACEHOLDER = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};

    public static PackBlock createBlockFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
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

        final boolean hasRecipe = reader.hasChild("recipes");

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "\\" + name + ".name", title);

        return new PackBlock(pack, name, textureName, hardness, dropAmount, resistance, rotation, mirrorRotation, lightLevel, lightOpacity,
                             showInCreativeTab, creativeTabName, textureCoordinatesByFace, shapeName, renderAsNormalBlock, renderAsOpaque, hasRecipe);
    }

    public static PackItem createItemFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
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

        final boolean hasRecipe = reader.hasChild("recipes");

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + name + ".name", title);

        return new PackItem(pack, name, titleLines.length == 1 ? null : Arrays.copyOfRange(titleLines, 1, titleLines.length), textureName, shapeName,
                            textureCoordinatesByFace, showInCreativeTab, creativeTabName, hasRecipe);
    }

    public static PackFood createFoodFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
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

        final boolean hasRecipe = reader.hasChild("recipes");

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + name + ".name", title);

        return new PackFood(pack, name, titleLines.length == 1 ? null : Arrays.copyOfRange(titleLines, 1, titleLines.length), textureName, shapeName,
                            textureCoordinatesByFace, showInCreativeTab, creativeTabName, healAmount,
                            saturationModifier, isWolfFavorite, alwaysEdible, hasRecipe);
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
            final String type = entry.getValue().getChild("type").getString("").toUpperCase();
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
        final int amount = node.getChild("amount").getInt(1);
        final int damage = node.getChild("damage").getInt(0);
        final List<Object> params = Lists.newArrayList();

        for (String itemsRaw : node.getChild("ingredients").getStringList()) {
            final String[] itemsSplit = itemsRaw.split(" ");
            for (String identifierCombined : itemsSplit) {
                final String[] separated = identifierCombined.split(StringEscapeUtils.escapeJava("\\"));
                final String modid = separated[0].toLowerCase();
                String identifier;
                if (separated.length > 1) {
                    identifier = identifierCombined.split(modid + StringEscapeUtils.escapeJava("\\"))[1].toLowerCase();
                } else {
                    identifier = modid;
                }
                boolean minecraft = modid.equalsIgnoreCase("Minecraft") || modid.equals(identifier);
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
            int index = 0;
            final Map<Object, Character> objectViaParamMap = Maps.newHashMap();

            final List<Object> combinedParams = Lists.newArrayList();

            //aaa
            //bbb
            //ccc
            for (int i = 0; i < params.size(); i++) {
                Character c = objectViaParamMap.get(param);
                if (c == null) {
                    c = RECIPE_MATRIX_PLACEHOLDER[index];
                    objectViaParamMap.put(param, c);
                    index++;
                }
                combinedParams.add(c);
            }
            for (Map.Entry<Object, Character> entry : objectViaParamMap.entrySet()) {
                combinedParams.add(entry.getValue());
                combinedParams.add(entry.getKey());
            }

            if (shaped) {
                if (itemResult) {
                    GameRegistry.addShapedRecipe(new ItemStack(GameRegistry.findItem("almura", pack.getName() + "\\" + name), amount, damage), combinedParams.toArray());
                } else {
                    GameRegistry.addShapedRecipe(new ItemStack(GameRegistry.findBlock("almura", pack.getName() + "\\" + name), amount, damage), combinedParams.toArray());
                }
            } else {
                if (itemResult) {
                    GameRegistry.addShapelessRecipe(new ItemStack(GameRegistry.findItem("almura", pack.getName() + "\\" + name), amount, damage), combinedParams.toArray());
                } else {
                    GameRegistry.addShapelessRecipe(new ItemStack(GameRegistry.findBlock("almura", pack.getName() + "\\" + name), amount, damage), combinedParams.toArray());
                }
            }
        }
    }

    public static PackCrops createCropFromReader(Pack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild("title").getString(name).split("\\n")[0];
        String textureName = reader.getChild("texture").getString(name);
        textureName = textureName.split(".png")[0];

        final int levelRequired = reader.getChild("level-required").getInt(Integer.MIN_VALUE);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "\\" + name + ".name", title);

        return null;
    }

    public static PackSeeds createSeedFromNode(Pack pack, Block soil, PackCrops crop, String textureName, ConfigurationNode node)
            throws ConfigurationException {
        final String identifier = node.getChild("identifier").getString();
        if (identifier.isEmpty()) {
            throw new ConfigurationException("Providing an empty identifier for crop [" + crop + "] seed is not allowed!");
        }
        final String title = node.getChild("title").getString(crop.getLocalizedName()).split("\\n")[0];
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(node.getChild("coords"));
        final boolean showInCreativeTab = node.getChild("show-in-creative-tab").getBoolean(true);
        final String creativeTabName = node.getChild("creative-tab-name").getString("other");
        final boolean dropOnGrassBreak = node.getChild("drop-on-grass-break").getBoolean(true);

        //LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "\\" + crop.getLocalizedName() + )

        return new PackSeeds(pack, crop.getUnlocalizedName() + "\\", textureName, showInCreativeTab, creativeTabName, crop, soil);
    }

    public static Stage createStageFromNode(Pack pack, PackCrops crop, int id, ConfigurationNode node) {
        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(node.getChild("coords"));
        String shapeName = node.getChild("shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

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

        return null;
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
