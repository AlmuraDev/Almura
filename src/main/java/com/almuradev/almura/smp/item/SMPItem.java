package com.almuradev.almura.smp.item;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.smp.SMPIcon;
import com.almuradev.almura.smp.SMPPack;
import com.almuradev.almura.smp.SMPUtil;
import com.almuradev.almura.smp.model.SMPShape;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.common.registry.GameRegistry;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import java.util.List;
import java.util.Map;

public class SMPItem extends Item {
    private final SMPPack pack;
    //TEXTURES
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    public ClippedIcon[] clippedIcons;
    //SHAPES
    private SMPShape shape;
    private final String shapeName;

    public SMPItem(SMPPack pack, String identifier, String textureName, String shapeName, Map<Integer, List<Integer>> textureCoordinatesByFace, boolean showInCreativeTab, String creativeTabName) {
        this.pack = pack;
        this.textureCoordinatesByFace = textureCoordinatesByFace;
        this.shapeName = shapeName;
        setUnlocalizedName(pack.getName() + "_" + identifier);
        setTextureName(textureName);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
        GameRegistry.registerItem(this, pack.getName() + "_" + identifier);
    }

    public static SMPItem createFromReader(SMPPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild("Title").getString(name);
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final boolean showInCreativeTab = reader.getChild("show-in-creative-tab").getBoolean(true);
        final String creativeTabName = reader.getChild("creative-tab-name").getString("legacy");

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = SMPUtil.extractCoordsFrom(reader);

        Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, "item." + pack.getName() + "_" + name + ".name", title);

        return new SMPItem(pack, name, textureName, shapeName, textureCoordinatesByFace, showInCreativeTab, creativeTabName);
    }

    @Override
    public void registerIcons(IIconRegister register) {
        if (textureCoordinatesByFace.isEmpty()) {
            super.registerIcons(register);
            return;
        }

        itemIcon = new SMPIcon(pack, iconString).register((TextureMap) register);

        clippedIcons = SMPUtil.generateClippedIconsFromCoords(pack, itemIcon, iconString, textureCoordinatesByFace);
    }

    public SMPPack getPack() {
        return pack;
    }

    public SMPShape getShape() {
        return shape;
    }

    public void reloadShape() {
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
    public String toString() {
        return "SMPItem {pack= " + pack.getName() + ", raw_name= " + getUnlocalizedName() + "}";
    }
}
