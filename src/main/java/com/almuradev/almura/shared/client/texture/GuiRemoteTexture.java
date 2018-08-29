/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.texture;

import com.almuradev.almura.asm.ClientStaticAccess;
import net.malisis.core.client.gui.GuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@SideOnly(Side.CLIENT)
public class GuiRemoteTexture extends GuiTexture {

    private final String targetUrl;

    // TODO: Caching
    public GuiRemoteTexture(ResourceLocation defaultResourceLocation, ResourceLocation targetResourceLocation, String targetUrl, int width, int
            height) {
        super(defaultResourceLocation, width, height);
        this.targetUrl = targetUrl;

        CompletableFuture.supplyAsync(() -> {
            HttpURLConnection httpURLConnection = null;

            try {
                httpURLConnection = (HttpURLConnection) (new URL(targetUrl).openConnection(Minecraft.getMinecraft().getProxy()));
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, "
                        + "like Gecko) Chrome/26.0.1410.65 Safari/537.31");
                httpURLConnection.connect();

                if (httpURLConnection.getResponseCode() / 100 == 2) {
                    return httpURLConnection.getInputStream();
                }
            } catch (IOException e) {
                ClientStaticAccess.logger.warn("Malformed stream", e);
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return null;
        }).thenAccept(stream -> {
            final InputStream effStream = stream;

            if (stream == null) {
                ClientStaticAccess.logger.error("The specified resource at [" + this.targetUrl + "] could not be found.");
            }

            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    this.resourceLocation = registerTexture(targetResourceLocation, effStream);
                } catch (IOException e) {
                    ClientStaticAccess.logger.warn("Unable to register texture from [" + this.targetUrl + "]", e);
                }
            });
        });
    }

    private ResourceLocation registerTexture(ResourceLocation location, InputStream stream) throws IOException {
        final BufferedImage image = TextureUtil.readBufferedImage(stream);
        Minecraft.getMinecraft().getTextureManager().loadTexture(location, new BufferedTexture(location, image));
        return location;
    }
}
