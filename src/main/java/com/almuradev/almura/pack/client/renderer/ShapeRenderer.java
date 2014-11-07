/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.client.renderer;

import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.item.PackFood;
import com.almuradev.almura.pack.item.PackItem;
import com.almuradev.almura.pack.model.PackFace;
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

        if (block instanceof PackBlock) {
            shape = ((PackBlock) block).getShape();
        } else if (itemStack != null) {
            final Item item = itemStack.getItem();
            if (item instanceof PackItem) {
                shape = ((PackItem) item).getShape();
            } else if (item instanceof PackFood) {
                shape = ((PackFood) item).getShape();
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

            if (!(f instanceof PackFace)) {
                super.applyTexture(shape, parameters);
                return;
            }

            IIcon icon = null;

            if (block != null) {
                if (((PackBlock) block).clippedIcons == null) {
                    icon = super.getIcon(params);
                } else if (((PackFace) f).getTextureId() >= ((PackBlock) block).clippedIcons.length) {
                    icon = ((PackBlock) block).clippedIcons[0];
                } else {
                    icon = ((PackBlock) block).clippedIcons[((PackFace) f).getTextureId()];
                    if (icon == null) {
                        icon = ((PackBlock) block).clippedIcons[0];
                    }
                }
            } else if (itemStack != null) {
                final Item item = itemStack.getItem();
                if (item instanceof PackItem) {
                    if (((PackItem) item).clippedIcons == null) {
                        icon = super.getIcon(params);
                    } else if (((PackFace) f).getTextureId() >= ((PackItem) item).clippedIcons.length) {
                        icon = ((PackItem) item).clippedIcons[0];
                    } else {
                        icon = ((PackItem) item).clippedIcons[((PackFace) f).getTextureId()];
                        if (icon == null) {
                            icon = ((PackItem) item).clippedIcons[0];
                        }
                    }
                } else if (item instanceof PackFood) {
                    if (((PackFood) item).clippedIcons == null) {
                        icon = super.getIcon(params);
                    } else if (((PackFace) f).getTextureId() >= ((PackFood) item).clippedIcons.length) {
                        icon = ((PackFood) item).clippedIcons[0];
                    } else {
                        icon = ((PackFood) item).clippedIcons[((PackFace) f).getTextureId()];
                        if (icon == null) {
                            icon = ((PackFood) item).clippedIcons[0];
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
