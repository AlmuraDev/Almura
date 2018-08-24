/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.flower.state;

import com.almuradev.content.type.block.AbstractSingleBlockStateDefinition;
import com.almuradev.content.type.block.type.horizontal.component.aabb.HorizontalBox;

public final class FlowerBlockStateDefinition extends AbstractSingleBlockStateDefinition<HorizontalBox, HorizontalBox.Collision, HorizontalBox> {
    FlowerBlockStateDefinition(final FlowerBlockStateDefinitionBuilderImpl builder) {
        super(builder);
    }
}
