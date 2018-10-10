/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public final class DirectoryResourcePack implements IResourcePack {
    private final AbstractFileSystemSearchEntry entry;

    public DirectoryResourcePack(final AbstractFileSystemSearchEntry entry) {
        this.entry = entry;
    }

    @Override
    public boolean resourceExists(final ResourceLocation location) {
        return Files.exists(this.entry.path.resolve(location.getNamespace()).resolve(location.getPath()));
    }

    @Override
    public InputStream getInputStream(final ResourceLocation location) throws IOException {
        return Files.newInputStream(this.entry.path.resolve(location.getNamespace()).resolve(location.getPath()));
    }

    @Override
    public Set<String> getResourceDomains() {
        return this.entry.namespaces();
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(final MetadataSerializer serializer, final String section) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        throw new IOException("unsupported");
    }

    @Override
    public String getPackName() {
        return this.entry.id;
    }
}
