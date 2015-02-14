/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Represents an attachment, "accessory", that is applied to an {@link net.minecraft.entity.player.EntityPlayer} at the end of the render cycle.
 */
public interface IAccessory<M extends ModelBase> {

    @SideOnly(Side.CLIENT)
    void onAttached(EntityPlayer player, ResourceLocation textureLocation, M root);

    @SideOnly(Side.CLIENT)
    void onRender(EntityPlayer player, ResourceLocation textureLocation, M root, float partialTicks);

    String getName();
}
