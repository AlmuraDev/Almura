/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer;

import com.almuradev.almura.blocks.yaml.YamlBlock;
import com.almuradev.almura.resource.SMPFace;
import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class ShapeRenderer extends BaseRenderer {
    private Cube cube;

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    protected void initShapes() {
        cube = new Cube();
    }

    @Override
    public void render() {
        final Shape shape = ((YamlBlock) block).getShape();
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
        drawShape(shape, rp);
    }

    @Override
    public void applyTexture(Shape shape, RenderParameters parameters) {
        for (Face f : shape.getFaces()) {
            final RenderParameters params = RenderParameters.merge(f.getParameters(), parameters);
            IIcon icon;

            if (!(f instanceof SMPFace)) {
                super.applyTexture(shape, parameters);
                return;
            }

            if (((YamlBlock) block).textureIcons == null) {
               icon = super.getIcon(params);
            } else if (((SMPFace) f).getTextureId() >= ((YamlBlock) block).textureIcons.length) {
                icon = ((YamlBlock) block).textureIcons[0];
            } else {
                icon = ((YamlBlock) block).textureIcons[((SMPFace) f).getTextureId()];
                if (icon == null) {
                    icon = ((YamlBlock) block).textureIcons[0];
                }
            }

            if (icon != null) {
                boolean flipU = params.flipU.get();
                if (params.direction.get() == ForgeDirection.NORTH || params.direction.get() == ForgeDirection.EAST)
                    flipU = !flipU;
                f.setTexture(icon, flipU, params.flipV.get(), params.interpolateUV.get());
            }
        }
    }
}
