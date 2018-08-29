/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.texture;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;

@SideOnly(Side.CLIENT)
public final class BufferedTexture extends SimpleTexture {

    private final BufferedImage image;

    public BufferedTexture(final ResourceLocation key, final BufferedImage image) {
        super(key);
        this.image = image;
    }

    @Override
    public void loadTexture(final IResourceManager resourceManager) {
        TextureUtil.uploadTextureImageAllocate(getGlTextureId(), this.image, false, false);
    }
}
