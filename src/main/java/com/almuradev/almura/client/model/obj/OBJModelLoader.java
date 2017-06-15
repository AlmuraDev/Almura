/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.obj;

import com.almuradev.almura.Almura;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OBJModelLoader implements ICustomModelLoader {

    public static final OBJModelLoader instance = new OBJModelLoader();

    private final Set<String> domains = new HashSet<>();
    private final Map<ResourceLocation, IModel> cache = new HashMap<>();
    private final Map<ResourceLocation, Exception> errors = new HashMap<>();

    private IResourceManager resourceManager;

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return this.domains.contains(modelLocation.getResourceDomain()) && modelLocation.getResourcePath().endsWith(".obj");
    }

    @Nullable
    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        // Suppress the first model loading exception (like Forge does in their loader)
        final Exception exception = this.errors.get(modelLocation);

        if (exception != null) {
            throw new ModelLoaderRegistry.LoaderException("Failed to load model [" + modelLocation + "]!", exception);
        }

        IModel cached = this.cache.get(modelLocation);

        if (cached == null) {
            final IResource model = this.resourceManager.getResource(modelLocation);

            final OBJModelParser parser = new OBJModelParser(this.resourceManager, modelLocation, model);

            try {
                cached = parser.parse();
            } catch (Exception ex) {
                this.errors.put(modelLocation, ex);
            } finally {
                this.cache.put(modelLocation, cached);
            }
        }

        return cached;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        Almura.instance.logger.debug("Reloading OBJ model store.");
        this.resourceManager = resourceManager;
        this.cache.clear();
        this.errors.clear();
    }

    public void registerDomain(String domain) {
        this.domains.add(domain);

        Almura.instance.logger.info("Registered domain [" + domain + "] for OBJ loading.");
    }
}
