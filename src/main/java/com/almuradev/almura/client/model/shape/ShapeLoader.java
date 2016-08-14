/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.shape;

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

    private final Map<ResourceLocation, AbstractShapeModel> models = new HashMap<>();
    private IResourceManager manager;

    @Override
    public boolean accepts(ResourceLocation modelLocation) {

        // Only Almura would ever load shapes. No one else is fucking retarded enough to use this format...
        return modelLocation.getResourceDomain().equals(Almura.PLUGIN_ID) && modelLocation.getResourcePath().endsWith(".shape");
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
                    model = parser.parse(resource);
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
