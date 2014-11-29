/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.renderer;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IModelContainer;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.model.MalisisModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;

public class PackModelRenderer extends MalisisRenderer {

    @Override
    public void render() {
        IModelContainer container = (IModelContainer) block;
        if (container.getModel() == null) {
            return;
        }

        final MalisisModel model = container.getModel();
        model.resetState();
        enableBlending();
        if (renderType == RenderType.ISBRH_INVENTORY) {
            model.scale(1f, 1f, 1f, 0f, 0f, 0f); //TODO Ask Ordinastie
            RenderHelper.enableStandardItemLighting();
        }
        rp.useBlockBounds.set(false); //fixes custom lights rendering the collision box, may be a problem in the future.
        model.render(this, rp);
        if (renderType == RenderType.ISBRH_INVENTORY) {
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerFor(Class... listClass) {
        for (Class clazz : listClass) {
            if (Block.class.isAssignableFrom(clazz) && IClipContainer.class.isAssignableFrom(clazz) && IModelContainer.class
                    .isAssignableFrom(clazz)) {
                super.registerFor(clazz);
            } else {
                Almura.LOGGER.error("Cannot register " + clazz.getSimpleName() + " for PackBlockRenderer!");
            }
        }
    }

    @Override
    public void registerFor(Item item) {
        throw new UnsupportedOperationException("PackModelRenderer is only meant for blocks!");
    }
}
