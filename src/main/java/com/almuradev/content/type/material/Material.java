/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.material;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.mapcolor.MapColor;
import net.minecraft.block.material.EnumPushReaction;

public interface Material extends CatalogedContent {
    interface Builder extends ContentBuilder<Material> {
        void blocksLight(final boolean blocksLight);

        void blocksMovement(final boolean blocksMovement);

        void liquid(final boolean liquid);

        void mapColor(final MapColor mapColor);

        void push(final EnumPushReaction push);

        void solid(final boolean solid);

        void replaceable(final boolean replaceable);

        void translucent(final boolean translucent);

        void toolRequired(final boolean toolRequired);
    }
}
