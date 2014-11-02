/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer;

import com.almuradev.almura.blocks.yaml.YamlBlock;
import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.Shape;

public class ShapeRenderer extends BaseRenderer {

    @Override
    public void render() {
        if (renderType == TYPE_TESR_WORLD || renderType == TYPE_ISBRH_WORLD) {
            final Shape shape = ((YamlBlock) block).getShape();
            if (shape == null) {
                renderStandard();
                return;
            }


            rp.interpolateUV.set(false);
            drawShape(shape);
        }
    }
}
