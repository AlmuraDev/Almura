/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj;

import com.almuradev.content.model.OnDemandModelLoader;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public class OBJModelLoader implements ICustomModelLoader, OnDemandModelLoader {
    private final Set<String> domains = new HashSet<>();
    private final Map<ResourceLocation, IModel> cache = new HashMap<>();
    private final Map<ResourceLocation, Exception> errors = new HashMap<>();
    private final Logger logger;
    private final OBJModelParser.Factory parserFactory;
    private IResourceManager resourceManager;

    @Inject
    private OBJModelLoader(final Logger logger, final OBJModelParser.Factory parserFactory) {
        this.logger = logger;
        this.parserFactory = parserFactory;
    }

    @Override
    public boolean accepts(final ResourceLocation modelLocation) {
        return this.domains.contains(modelLocation.getResourceDomain()) && modelLocation.getResourcePath().endsWith(".obj");
    }

    @Nullable
    @Override
    public IModel loadModel(final ResourceLocation modelLocation) throws Exception {
        // Suppress the first model loading exception (like Forge does in their loader)
        final Exception exception = this.errors.get(modelLocation);

        if (exception != null) {
            throw new ModelLoaderRegistry.LoaderException("Failed to load model [" + modelLocation + "]!", exception);
        }

        IModel cached = this.cache.get(modelLocation);

        if (cached == null) {
            final IResource model = this.resourceManager.getResource(modelLocation);

            final OBJModelParser parser = this.parserFactory.create(this.resourceManager, modelLocation, model);

            try {
                cached = parser.parse();
            } catch (final Exception ex) {
                this.errors.put(modelLocation, ex);
            } finally {
                this.cache.put(modelLocation, cached);
            }
        }

        return cached;
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {
        this.logger.debug("Reloading OBJ model store.");
        this.resourceManager = resourceManager;
        this.cache.clear();
        this.errors.clear();
    }

    @Override
    public void registerInterest(final String id) {
        this.logger.info("Registering domain [{}] for OBJ loading.", id);
        this.domains.add(id);
    }
}
