package com.almuradev.almura.client;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class BufferedTexture extends SimpleTexture {
    private final BufferedImage image;

    public BufferedTexture(ResourceLocation key, BufferedImage image) {
        super(key);
        this.image = image;
    }

    @Override
    public void loadTexture(IResourceManager p_110551_1_) throws IOException {
        TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, false, false);
    }
}
