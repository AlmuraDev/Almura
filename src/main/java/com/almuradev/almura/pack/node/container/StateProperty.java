/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.container;

import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.node.property.IProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import java.util.List;
import java.util.Map;

public class StateProperty implements IProperty<Boolean>, IPackObject, IBlockClipContainer, IBlockShapeContainer {

    private final Pack pack;
    private final boolean enabled;
    private final String identifier;
    private final String textureName;
    private final Map<Integer, List<Integer>> textureCoordinates;
    private final String shapeName;
    private PackShape shape;
    private ClippedIcon[] clippedIcons;
    private IIcon stateIcon;

    public StateProperty(Pack pack, boolean enabled, String identifier, String textureName, Map<Integer, List<Integer>> textureCoordinates,
                         String shapeName) {
        this.pack = pack;
        this.enabled = enabled;
        this.identifier = identifier;
        this.textureName = textureName;
        this.textureCoordinates = textureCoordinates;
        this.shapeName = shapeName;
    }

    @Override
    public Boolean getSource() {
        return enabled;
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTextureName() {
        return textureName;
    }

    public Map<Integer, List<Integer>> getTextureCoordinates() {
        return textureCoordinates;
    }

    @Override
    public PackShape getShape() {
        return shape;
    }

    @Override
    public void setShape(PackShape shape) {
        this.shape = shape;
    }

    public String getShapeName() {
        return shapeName;
    }

    @Override
    public PackShape getShape(IBlockAccess access, int x, int y, int z, int metadata) {
        return shape;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        stateIcon = new PackIcon(this, textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoordinates(stateIcon, textureName, textureCoordinates);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getStateIcon() {
        return stateIcon;
    }

    @Override
    public ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata) {
        return clippedIcons;
    }
}
