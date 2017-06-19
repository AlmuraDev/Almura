/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class CustomFolderResourcePack extends FolderResourcePack {
    private final String name;
    private final Set<String> availableNamespaces;

    public CustomFolderResourcePack(String name, Path root, String domain) {
        super(root.toFile());

        this.name = name;
        this.availableNamespaces = ImmutableSet.of(domain);
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return Files.exists(Paths.get(this.resourcePackFile.toString(), location.getResourcePath().split("/")));
    }

    @Nullable
    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        return Files.newInputStream(Paths.get(this.resourcePackFile.toString(), location.getResourcePath().split("/")));
    }

    @SuppressWarnings("unchecked")
    @Override
    public IMetadataSection getPackMetadata(MetadataSerializer par1MetadataSerializer, String par2Str) throws IOException {
        return null;
    }

    @Override
    public Set<String> getResourceDomains() {
        return this.availableNamespaces;
    }

    @Override
    public String getPackName() {
        return this.name;
    }
}
