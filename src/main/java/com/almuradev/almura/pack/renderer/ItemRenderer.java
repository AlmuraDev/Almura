/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.renderer;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IBlockModelContainer;
import com.almuradev.almura.pack.ITextureContainer;
import com.almuradev.almura.pack.IModelContainer;
import com.almuradev.almura.pack.model.IModel;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.google.common.base.Optional;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.shape.Cube;
import net.minecraft.item.Item;

public class ItemRenderer extends MalisisRenderer {

    private Cube cubeModel;

    @Override
    protected void initialize() {
        cubeModel = new Cube();
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public void render() {
        shape = cubeModel;
        final Optional<PackModelContainer>
                modelContainer = ((IBlockModelContainer) block).getModelContainer(world, x, y, z, blockMetadata);
        if (modelContainer.isPresent()) {
            if (modelContainer.get().getModel().isPresent()) {
                final IModel model = modelContainer.get().getModel().get();
                if (model instanceof PackModelContainer.PackShape) {
                    shape = (PackModelContainer.PackShape) model;
                }
            }
        }
        shape.resetState();
        enableBlending();
        rp.interpolateUV.set(false);
        rp.flipU.set(true);
        rp.flipV.set(true);

        if (renderType == RenderType.ITEM_INVENTORY) {
            shape.scale(1, 1, 1);
        }

        drawShape(shape, rp);
    }

    @SuppressWarnings("rawtypes")
	@Override
    public void registerFor(Class... listClass) {
        for (Class clazz : listClass) {
            if (Item.class.isAssignableFrom(clazz) && ITextureContainer.class.isAssignableFrom(clazz) && IModelContainer.class
                    .isAssignableFrom(clazz)) {
                super.registerFor(clazz);
            } else {
                Almura.LOGGER.error("Cannot register " + clazz.getSimpleName() + " for " + getClass().getSimpleName());
            }
        }
    }

    @Override
    public void registerFor(Item item) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is only meant for items!");
    }
}
