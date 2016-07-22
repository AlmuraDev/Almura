/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.client.model.shape;

import com.almuradev.almura.Almura;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ShapeLoader implements ICustomModelLoader {

    private IResourceManager manager;
    private final Map<ResourceLocation, ShapeModel> models = new HashMap<>();

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        // Only Almura would ever load shapes. No one else is fucking retarded enough to use this format...
        return modelLocation.getResourceDomain().equals(Almura.PLUGIN_ID) && modelLocation.getResourcePath().endsWith(".shape");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {

        ShapeModel model = models.get(modelLocation);

        if (model == null) {
            IResource resource = null;

            try {
                resource = manager.getResource(modelLocation);
            } catch (IOException ioe) {
                Almura.instance.logger.error("An error occurred loading shape file [{}]!", modelLocation, ioe);
            }

            if (resource != null) {
                try {
                    final ShapeModel.Parser parser = new ShapeModel.Parser(resource);

                    model = parser.parse();
                    models.put(modelLocation, model);
                } catch (Exception ex) {
                    Almura.instance.logger.error("An error occurred loading shape [{}]!", modelLocation, ex);
                }
            }
        }

        return model;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.manager = resourceManager;
        this.models.clear();
    }
}
