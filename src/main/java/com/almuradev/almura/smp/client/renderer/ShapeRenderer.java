/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.smp.client.renderer;

import com.almuradev.almura.smp.SMPBlock;
import com.almuradev.almura.smp.item.SMPFood;
import com.almuradev.almura.smp.item.SMPItem;
import com.almuradev.almura.smp.model.SMPFace;
import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class ShapeRenderer extends BaseRenderer {

    private Cube cube;

    @Override
    protected void initialize() {
        cube = new Cube();
    }

    @Override
    public void render() {
        Shape shape = null;

        if (block instanceof SMPBlock) {
            shape = ((SMPBlock) block).getShape();
        } else if (itemStack != null) {
            final Item item = itemStack.getItem();
            if (item instanceof SMPItem) {
                shape = ((SMPItem) item).getShape();
            } else if (item instanceof SMPFood) {
                shape = ((SMPFood) item).getShape();
            }
        }

        if (shape == null) {
            drawShape(cube);
            return;
        }

        shape.resetState();
        enableBlending();
        rp.useNormals.set(true);
        rp.interpolateUV.set(false);
        rp.flipU.set(true);
        rp.flipV.set(true);
        rp.renderAllFaces.set(true);

        if (renderType == TYPE_ISBRH_INVENTORY) {
            shape.limit(0, 0, 0, 1, 1, 1);
        }
        drawShape(shape, rp);
    }

    @Override
    public void applyTexture(Shape shape, RenderParameters parameters) {
        for (Face f : shape.getFaces()) {
            final RenderParameters params = RenderParameters.merge(f.getParameters(), parameters);

            if (!(f instanceof SMPFace)) {
                super.applyTexture(shape, parameters);
                return;
            }

            IIcon icon = null;
            
            if (block != null) {
                if (((SMPBlock) block).clippedIcons == null) {
                    icon = super.getIcon(params);
                } else if (((SMPFace) f).getTextureId() >= ((SMPBlock) block).clippedIcons.length) {
                    icon = ((SMPBlock) block).clippedIcons[0];
                } else {
                    icon = ((SMPBlock) block).clippedIcons[((SMPFace) f).getTextureId()];
                    if (icon == null) {
                        icon = ((SMPBlock) block).clippedIcons[0];
                    }
                }
            } else if (itemStack != null) {
                final Item item = itemStack.getItem();
                if (item instanceof SMPItem) {
                    if (((SMPItem) item).clippedIcons == null) {
                        icon = super.getIcon(params);
                    } else if (((SMPFace) f).getTextureId() >= ((SMPItem) item).clippedIcons.length) {
                        icon = ((SMPItem) item).clippedIcons[0];
                    } else {
                        icon = ((SMPItem) item).clippedIcons[((SMPFace) f).getTextureId()];
                        if (icon == null) {
                            icon = ((SMPItem) item).clippedIcons[0];
                        }
                    }
                } else if (item instanceof SMPFood) {
                    if (((SMPFood) item).clippedIcons == null) {
                        icon = super.getIcon(params);
                    } else if (((SMPFace) f).getTextureId() >= ((SMPFood) item).clippedIcons.length) {
                        icon = ((SMPFood) item).clippedIcons[0];
                    } else {
                        icon = ((SMPFood) item).clippedIcons[((SMPFace) f).getTextureId()];
                        if (icon == null) {
                            icon = ((SMPFood) item).clippedIcons[0];
                        }
                    }
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
}
