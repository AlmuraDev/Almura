/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.renderer;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.RotationMeta;
import com.almuradev.almura.pack.model.PackFace;
import com.almuradev.almura.pack.model.PackMirrorFace;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.node.RotationNode;
import com.almuradev.almura.pack.node.property.RotationProperty;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRenderer extends MalisisRenderer {

    private Cube vanillaShape;

    @Override
    protected void initialize() {
        vanillaShape = new Cube();
    }

    @Override
    public void render() {
        enableBlending();
        Shape shape = ((IBlockShapeContainer) block).getShape(world, x, y, z, blockMetadata);

        if (shape == null) {
            shape = vanillaShape;
        }
        rp.useBlockBounds.set(false);
        if (shape instanceof PackShape) {
            rp.renderAllFaces.set(true);
            rp.flipU.set(true);
            rp.flipV.set(true);
            rp.interpolateUV.set(false);
        } else {
            rp.flipU.set(false);
            rp.flipV.set(false);
            rp.interpolateUV.set(true);
        }

        shape.resetState();

        handleRotation();

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
        if (Minecraft.getMinecraft().currentScreen == null) {
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
            final PackFace face = (PackFace) f;
            final ClippedIcon[] clippedIcons = ((IBlockClipContainer) block).getClipIcons(world, x, y, z, blockMetadata);
            IIcon icon;

            if (PackUtil.isEmptyClip(clippedIcons)) {
                icon = super.getIcon(params);
            } else if (face.getTextureId() >= clippedIcons.length) {
                icon = clippedIcons[0];
            } else {
                icon = clippedIcons[face.getTextureId()];
                if (icon == null) {
                    icon = clippedIcons[0];
                }
            }

            if (icon != null) {
                if (f instanceof PackMirrorFace) {
                    icon = ((MalisisIcon) icon).copy().flip(true, false);
                }

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
            if (Block.class.isAssignableFrom(clazz) && IBlockClipContainer.class.isAssignableFrom(clazz) && IBlockShapeContainer.class
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

    private void handleRotation() {
        if (renderType == RenderType.ISBRH_WORLD && block instanceof INodeContainer && shape instanceof PackShape) {
            final RotationNode node = ((INodeContainer) block).getNode(RotationNode.class);
            if (node == null) {
                return;
            }
            if (!node.isEnabled()) {
                return;
            }
            final RotationMeta.Rotation rotation = RotationMeta.Rotation.getState(blockMetadata);
            final RotationProperty property = node.getRotationProperty(rotation);
            if (property == null) {
                switch (rotation) {
                    case NORTH:
                        if (node.isDefaultRotate()) {
                            shape.rotate(180f, 0, -1, 0);
                        }
                        break;
                    case SOUTH:
                        break;
                    case WEST:
                        if (node.isDefaultRotate()) {
                            shape.rotate(90f, 0, -1, 0);
                        }
                        break;

                    case EAST:
                        if (node.isDefaultRotate()) {
                            shape.rotate(90f, 0, 1, 0);
                        }
                        break;

                    case DOWN_NORTH:
                        if (node.isDefaultMirrorRotate()) {
                            shape.rotate(90f, -1, 0, 0);
                            shape.rotate(180f, 0, -1, 0);
                        }
                        break;
                    case DOWN_SOUTH:
                        if (node.isDefaultMirrorRotate()) {
                            shape.rotate(180f, -1, 0, 0);
                            shape.rotate(90f, -1, 0, 0);
                        }
                        break;
                    case DOWN_WEST:
                        if (node.isDefaultMirrorRotate()) {
                            shape.rotate(180f, -1, 0, 0);
                            shape.rotate(90f, 0, -1, 0);
                        }
                        break;
                    case DOWN_EAST:
                        if (node.isDefaultMirrorRotate()) {
                            shape.rotate(180f, -1, 0, 0);
                            shape.rotate(90f, 0, 1, 0);
                        }
                        break;
                    case UP_NORTH:
                        if (node.isDefaultMirrorRotate()) {
                            shape.rotate(180f, 0, -1, 0);
                        }
                        break;
                    case UP_SOUTH:
                        break;
                    case UP_WEST:
                        if (node.isDefaultMirrorRotate()) {
                            shape.rotate(90f, 0, -1, 0);
                        }
                        break;
                    case UP_EAST:
                        if (node.isDefaultMirrorRotate()) {
                            shape.rotate(90f, 0, 1, 0);
                        }
                        break;
                }
            } else {
                shape.rotate(property.getAngle(), property.getX().getId(), property.getY().getId(), property.getZ().getId());
            }
        }
    }
}
