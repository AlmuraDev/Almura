package com.almuradev.almura.blocks.yaml;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.items.BasicItemBlock;
import com.almuradev.almura.lang.Languages;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Represents a block created from a {@link YamlConfiguration}.
 */
public class YamlBlock extends Block {
    public static YamlBlock create(Path file) throws FileNotFoundException, ConfigurationException {
        if (!file.endsWith(".yml")) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.warn("Attempted to load a block from file that was not YAML: " + file);
            }
            return null;
        }

        final String fileName = file.toFile().getName().split(".yml")[0];
        return create(fileName, new FileInputStream(file.toFile()));
    }

    public static YamlBlock create(String name, InputStream stream) throws ConfigurationException {
        final YamlConfiguration reader = new YamlConfiguration(stream);
        reader.load();

        final String title = reader.getChild("Title").getString(name);
        final String textureName = reader.getChild("Texture").getString(name);
        final float hardness = reader.getChild("Hardness").getFloat(1f);
        final String shapeName = reader.getChild("Shape").getString();

        Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, "tile." + name + ".name", title);

        return new YamlBlock(name, textureName, hardness, 1f, 0, shapeName);
    }

    public final String shapeName;

    public YamlBlock(String identifier) {
        this(identifier, identifier, 1f, 1f, 0, null);
    }

    public YamlBlock(String identifier, String textureName) {
        this(identifier, textureName, 1f, 1f, 0, null);
    }

    public YamlBlock(String identifier, String textureName, float hardness) {
        this(identifier, textureName, hardness, 1f, 0, null);
    }

    public YamlBlock(String identifier, String textureName, float hardness, float lightLevel) {
        this(identifier, textureName, hardness, lightLevel, 0, null);
    }

    public YamlBlock(String identifier, String textureName, float hardness, float lightLevel, int lightOpacity, String shapeName) {
        super(Material.rock);
        setBlockName(identifier);
        setBlockTextureName(Almura.MOD_ID + ":" + textureName);
        setHardness(hardness);
        setLightLevel(lightLevel);
        setLightOpacity(lightOpacity);
        setCreativeTab(Tabs.LEGACY);
        this.shapeName = shapeName;
        GameRegistry.registerBlock(this, BasicItemBlock.class, identifier);
    }
}
