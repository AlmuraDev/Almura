/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.model.PackShape;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.item.ItemSeeds;

public class PackSeeds extends ItemSeeds implements IPackObject, IClipContainer, IShapeContainer {

    private final Pack pack;
    private final String identifier;

    public PackSeeds(Pack pack, String identifier, String textureName, boolean showInCreativeTab,
                     String creativeTabName, Block crop, Block soil) {
        super(crop, soil);
        this.pack = pack;
        this.identifier = identifier;
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return new ClippedIcon[0];
    }

    @Override
    public PackShape getShape() {
        return null;
    }

    @Override
    public void setShape(PackShape shape) {

    }

    @Override
    public String getShapeName() {
        return null;
    }
}
