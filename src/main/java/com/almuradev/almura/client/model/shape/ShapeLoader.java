/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.shape;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ShapeLoader implements ICustomModelLoader {

    private final Map<ResourceLocation, AbstractShapeModel> models = new HashMap<>();
    private IResourceManager manager;

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourceDomain().equals(Constants.Plugin.ID) && modelLocation.getResourcePath().endsWith(".shape");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {

        AbstractShapeModel model = models.get(modelLocation);

        if (model == null) {
            IResource resource = null;

            try {
                resource = manager.getResource(modelLocation);
            } catch (IOException ioe) {
                Almura.instance.logger.error("An error occurred loading shape file [{}]!", modelLocation, ioe);
            }

            if (resource != null) {
                AbstractShapeModel.Parser parser;

                if (modelLocation.getResourcePath().contains("/parent/")) {
                    parser = new ParentShapeModel.Parser();
                } else {
                    parser = new ChildShapeModel.Parser();
                }
                try {
                    model = parser.parse(modelLocation, resource);
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
