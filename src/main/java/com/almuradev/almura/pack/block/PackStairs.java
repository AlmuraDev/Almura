/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IShapeContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.BlockStairs;

public class PackStairs extends BlockStairs implements IClipContainer, IShapeContainer {
    public static int renderId = 10;
    private final PackBlock wrapped;

    public PackStairs(PackBlock block) {
        super(block, 0);
        wrapped = block;
        setLightOpacity(block.getLightOpacity());
        setCreativeTab(block.getCreativeTabToDisplayOn());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return renderId;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return new ClippedIcon[0];
    }

    @Override
    public Shape getShape() {
        return null;
    }

    @Override
    public void setShapeFromPack() {

    }

    @Override
    public ContentPack getPack() {
        return null;
    }

    @Override
    public String toString() {
        return "PackStairs {pack= " + wrapped.getPack().getName() + ", raw_name= " + getUnlocalizedName() + ", source_block= " + wrapped + "}";
    }
}
