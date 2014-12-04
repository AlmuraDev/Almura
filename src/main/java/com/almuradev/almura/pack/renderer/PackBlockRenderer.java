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
import com.almuradev.almura.pack.RotationMeta;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.model.PackFace;
import com.almuradev.almura.pack.model.PackMirrorFace;
import com.almuradev.almura.pack.model.PackShape;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class PackBlockRenderer extends MalisisRenderer {

    private Cube vanillaShape;

    @Override
    protected void initialize() {
        vanillaShape = new Cube();
    }

    @Override
    public void render() {
        enableBlending();
        PackShape shape = ((IShapeContainer) block).getShape();

        if (shape == null) {
            rp.renderAllFaces.set(block.renderAsNormalBlock());
            rp.useBlockBounds.set(true);
            rp.interpolateUV.set(true);
            vanillaShape.resetState();
            super.drawShape(vanillaShape, rp);
            return;
        } else {
            rp.renderAllFaces.set(true);
            rp.useBlockBounds.set(false); //fixes custom lights rendering the collision box, may be a problem in the future.
        }

        enableBlending();
        rp.flipU.set(true);
        rp.flipV.set(true);
        rp.interpolateUV.set(false);

        shape.resetState();

        if (renderType == RenderType.ISBRH_WORLD) {
            switch (RotationMeta.getRotationFromMeta(blockMetadata)) {
                case NORTH:
                    if (((PackBlock) block).canRotate()) {
                        shape.rotate(180f, 0, -1, 0);
                        break;
                    }
                case SOUTH:
                    break;
                case WEST:
                    if (((PackBlock) block).canRotate()) {
                        shape.rotate(90f, 0, -1, 0);
                        break;
                    }
                case EAST:
                    if (((PackBlock) block).canRotate()) {
                        shape.rotate(90f, 0, 1, 0);
                        break;
                    }
                case DOWN_NORTH:
                    if (((PackBlock) block).canMirrorRotate()) {
                        shape.rotate(90f, -1, 0, 0);
                        shape.rotate(180f, 0, -1, 0);
                    }
                    break;
                case DOWN_SOUTH:
                    if (((PackBlock) block).canMirrorRotate()) {
                        shape.rotate(180f, -1, 0, 0);
                        shape.rotate(90f, -1, 0, 0);
                    }
                    break;
                case DOWN_WEST:
                    if (((PackBlock) block).canMirrorRotate()) {
                        shape.rotate(180f, -1, 0, 0);
                        shape.rotate(90f, 0, -1, 0);
                    }
                    break;
                case DOWN_EAST:
                    if (((PackBlock) block).canMirrorRotate()) {
                        shape.rotate(180f, -1, 0, 0);
                        shape.rotate(90f, 0, 1, 0);
                    }
                    break;
                case UP_NORTH:
                    if (((PackBlock) block).canMirrorRotate()) {
                        shape.rotate(180f, 0, -1, 0);
                    }
                    break;
                case UP_SOUTH:
                    break;
                case UP_WEST:
                    if (((PackBlock) block).canMirrorRotate()) {
                        shape.rotate(90f, 0, -1, 0);
                    }
                    break;
                case UP_EAST:
                    if (((PackBlock) block).canMirrorRotate()) {
                        shape.rotate(90f, 0, 1, 0);
                    }
                    break;
            }
        }
        if (renderType == RenderType.ISBRH_INVENTORY) {
            double max = Double.MIN_VALUE;
            for (Face fe : shape.getFaces()) {
                for (Vertex vt : shape.getVertexes(fe)) {
                    max = Math.max(vt.getX(), max);
                    max = Math.max(vt.getY(), max);
                    max = Math.max(vt.getZ(), max);
                }
            }
            shape.scale((float) (1 / max));
            RenderHelper.enableStandardItemLighting();
        }
        drawShape(shape, rp);
        if (renderType == RenderType.ISBRH_INVENTORY) {
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Override
    public void applyTexture(Shape shape, RenderParameters parameters) {
        if (!(shape instanceof PackShape)) {
            super.applyTexture(shape, parameters);
            return;
        }
        
        for (Face f : shape.getFaces()) {
            final RenderParameters params = RenderParameters.merge(f.getParameters(), parameters);
            final IClipContainer clipContainer = (IClipContainer) block;
            final PackFace face = (PackFace) f;
            IIcon icon;

            if (PackUtil.isEmpty(clipContainer)) {
                icon = super.getIcon(params);
            } else if (face.getTextureId() >= clipContainer.getClipIcons().length) {
                icon = clipContainer.getClipIcons()[0];
            } else {
                icon = clipContainer.getClipIcons()[face.getTextureId()];
                if (icon == null) {
                    icon = clipContainer.getClipIcons()[0];
                }
            }

            if (f instanceof PackMirrorFace) {
                icon = ((MalisisIcon) icon).copy().flip(true, false);
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
            if (clazz == PackBlock.class) {
                super.registerFor(clazz);
            } else {
                Almura.LOGGER.error("Cannot register " + clazz.getSimpleName() + " for PackBlockRenderer!");
            }
        }
    }

    @Override
    public void registerFor(Item item) {
        throw new UnsupportedOperationException("PackBlockRenderer is only meant for blocks!");
    }
}
