/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IModelContainer;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackModel;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.renderer.model.MalisisModel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

public class PackModelBlock extends Block implements IClipContainer, IModelContainer {

    public static int renderId;
    private final ContentPack pack;
    private final String textureName;
    private final String modelName;
    private PackModel model;
    private ClippedIcon[] clippedIcons;

    public PackModelBlock(ContentPack pack, String identifier, String textureName, String modelName) {
        super(Material.rock);
        this.pack = pack;
        this.textureName = textureName;
        this.modelName = modelName;
        setBlockName(pack.getName() + "_" + identifier);
        setBlockTextureName(Almura.MOD_ID.toLowerCase() + ":packs/" + pack.getName() + "//" + textureName);
    }

    public static PackModelBlock createFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        String modelName = reader.getChild("Model").getString();
        if (modelName != null) {
            modelName = modelName.split(".model")[0];
        }

        return new PackModelBlock(pack, name, textureName, modelName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = new PackIcon(pack.getName(), textureName).register((TextureMap) register);
        //clippedIcons = PackUtil.generateClippedIconsFromCoords(pack, blockIcon, textureName, textureCoordinatesByFace);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        if (PackUtil.isEmpty(clippedIcons)) {
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
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @Override
    public MalisisModel getModel() {
        return model;
    }

    @Override
    public void setModelFromPack() {
        this.model = null;

        for (PackModel model : pack.getModels()) {
            if (model.getName().equalsIgnoreCase(modelName)) {
                this.model = model;
                break;
            }
        }
    }

    @Override
    public ContentPack getPack() {
        return pack;
    }
}
