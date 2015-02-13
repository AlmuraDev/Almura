/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.renderer;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockModelContainer;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.RotationMeta;
import com.almuradev.almura.pack.model.IModel;
import com.almuradev.almura.pack.model.PackFace;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.pack.node.RotationNode;
import com.almuradev.almura.pack.node.property.RotationProperty;
import com.google.common.base.Optional;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRenderer extends MalisisRenderer {

    private Cube cubeModel;

    @Override
    protected void initialize() {
        cubeModel = new Cube();
    }

    @Override
    public void render() {
        shape = cubeModel;
        final Optional<PackModelContainer> modelContainer = ((IBlockModelContainer) block).getModelContainer(world, x, y, z, blockMetadata);
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
        rp.useBlockBounds.set(false);
        if (shape instanceof IModel) {
            rp.renderAllFaces.set(true);
            rp.flipU.set(true);
            rp.flipV.set(true);
            rp.interpolateUV.set(false);
        } else {
            rp.flipU.set(false);
            rp.flipV.set(false);
            rp.interpolateUV.set(true);
        }

        if (shape instanceof IModel) {
            if (renderType == RenderType.ISBRH_WORLD && block instanceof INodeContainer) {
                handleRotation((IModel) shape);
            }

            if (renderType == RenderType.ISBRH_INVENTORY) {
                handleScaling((IModel) shape);
            }
        }
        drawShape(shape, rp);
    }

    @Override
    protected IIcon getIcon(RenderParameters params) {
        if (face != null) {
            final PackFace pface = (PackFace) face;
            final ClippedIcon[] clippedIcons;

            if (world != null) {
                clippedIcons = ((IBlockClipContainer) block).getClipIcons(world, x, y, z, blockMetadata);
            } else {
                clippedIcons = ((IClipContainer) block).getClipIcons();
            }

            if (pface.getTextureId() > clippedIcons.length) {
                params.icon.set(clippedIcons[0]);
            } else {
                params.icon.set(clippedIcons[pface.getTextureId()]);
            }
        }
        return super.getIcon(params);
    }

    @Override
    public void applyTexture(Shape shape, RenderParameters parameters) {
        //shape.applyMatrix();
        for (Face f : shape.getFaces()) {
            face = f;
            RenderParameters params = RenderParameters.merge(f.getParameters(), parameters);
            IIcon icon = getIcon(params);
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
            if (Block.class.isAssignableFrom(clazz) && IBlockClipContainer.class.isAssignableFrom(clazz) && IBlockModelContainer.class
                    .isAssignableFrom(clazz) && INodeContainer.class.isAssignableFrom(clazz)) {
                super.registerFor(clazz);
            } else {
                Almura.LOGGER.error("Cannot register [" + clazz.getSimpleName() + "] for " + getClass().getSimpleName());
            }
        }
    }

    @Override
    public void registerFor(Item item) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is only meant for blocks.");
    }

    private void handleRotation(IModel model) {
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
                        model.rotate(180f, 0, -1, 0);
                    }
                    break;
                case SOUTH:
                    break;
                case WEST:
                    if (node.isDefaultRotate()) {
                        model.rotate(90f, 0, -1, 0);
                    }
                    break;
                case EAST:
                    if (node.isDefaultRotate()) {
                        model.rotate(90f, 0, 1, 0);
                    }
                    break;
                case DOWN_NORTH:
                    if (node.isDefaultRotate()) {
                        model.rotate(180f, 0, -1, 0);
                    }
                    if (node.isDefaultMirrorRotate()) {
                        model.rotate(90f, -1, 0, 0);
                    }
                    break;
                case DOWN_SOUTH:
                    break;
                case DOWN_WEST:
                    if (node.isDefaultRotate()) {
                        model.rotate(90f, 0, -1, 0);
                    }
                    if (node.isDefaultMirrorRotate()) {
                        model.rotate(180f, -1, 0, 0);
                    }
                    break;
                case DOWN_EAST:
                    if (node.isDefaultRotate()) {
                        model.rotate(90f, 0, 1, 0);
                    }
                    if (node.isDefaultMirrorRotate()) {
                        model.rotate(180f, -1, 0, 0);
                    }
                    break;
                case UP_NORTH:
                    if (node.isDefaultRotate()) {
                        model.rotate(180f, 0, -1, 0);
                    }
                    break;
                case UP_SOUTH:
                    break;
                case UP_WEST:
                    if (node.isDefaultRotate()) {
                        model.rotate(90f, 0, -1, 0);
                    }
                    break;
                case UP_EAST:
                    if (node.isDefaultRotate()) {
                        model.rotate(90f, 0, 1, 0);
                    }
                    break;
            }
        } else {
            model.rotate(property.getAngle(), property.getX().getId(), property.getY().getId(), property.getZ().getId());
        }
    }

    private void handleScaling(IModel model) {
        double max = Double.MIN_VALUE;
        for (Face fe : model.getFaces()) {
            for (Vertex vt : model.getVertexes(fe)) {
                max = Math.max(vt.getX(), max);
                max = Math.max(vt.getY(), max);
                max = Math.max(vt.getZ(), max);
            }
        }
        model.scale((float) (1 / max));
        RenderHelper.enableStandardItemLighting();

    }
}
