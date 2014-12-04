/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.renderer;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackFace;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemRenderer extends MalisisRenderer {

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public void render() {
        final Shape shape = ((IShapeContainer) itemStack.getItem()).getShape();
        shape.resetState();
        enableBlending();
        rp.interpolateUV.set(false);
        rp.flipU.set(true);
        rp.flipV.set(true);

        if (renderType == RenderType.ITEM_INVENTORY) {
            shape.scale(1, 1, 1);
            RenderHelper.enableStandardItemLighting();
        }

        drawShape(shape, rp);

        if (renderType == RenderType.ITEM_INVENTORY) {
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Override
    public void applyTexture(Shape shape, RenderParameters parameters) {
        for (Face f : shape.getFaces()) {
            final RenderParameters params = RenderParameters.merge(f.getParameters(), parameters);
            final IClipContainer clipContainer = (IClipContainer) itemStack.getItem();
            final PackFace face = (PackFace) f;
            IIcon icon;

            if (PackUtil.isEmpty(clipContainer.getClipIcons())) {
                icon = super.getIcon(params);
            } else if (face.getTextureId() >= clipContainer.getClipIcons().length) {
                icon = clipContainer.getClipIcons()[0];
            } else {
                icon = clipContainer.getClipIcons()[face.getTextureId()];
                if (icon == null) {
                    icon = clipContainer.getClipIcons()[0];
                }
            }

            if (icon != null) {
                boolean flipU = params.flipU.get();
                if (params.direction.get() == ForgeDirection.NORTH || params.direction.get() == ForgeDirection.EAST) {
                    flipU = !flipU;
                }
                f.setTexture(icon, flipU, params.flipV.get(), params.interpolateUV.get());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerFor(Class... listClass) {
        for (Class clazz : listClass) {
            if (Item.class.isAssignableFrom(clazz) && IClipContainer.class.isAssignableFrom(clazz) && IShapeContainer.class
                    .isAssignableFrom(clazz)) {
                super.registerFor(clazz);
            } else {
                Almura.LOGGER.error("Cannot register " + clazz.getSimpleName() + " for " + BlockRenderer.class.getSimpleName());
            }
        }
    }

    @Override
    public void registerFor(Item item) {
        throw new UnsupportedOperationException(BlockRenderer.class.getSimpleName() + " is only meant for blocks!");
    }
}
