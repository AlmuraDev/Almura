/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.material;

import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.mapcolor.MapColor;
import net.minecraft.block.material.EnumPushReaction;

public final class MaterialBuilder extends ContentBuilder.Impl<Material> implements Material.Builder {

    private boolean blocksLight;
    private boolean blocksMovement;
    private boolean liquid;
    private MapColor mapColor;
    private EnumPushReaction push;
    private boolean solid;
    private boolean replaceable;
    private boolean translucent;
    private boolean toolRequired;

    @Override
    public void blocksLight(final boolean blocksLight) {
        this.blocksLight = blocksLight;
    }

    @Override
    public void blocksMovement(final boolean blocksMovement) {
        this.blocksMovement = blocksMovement;
    }

    @Override
    public void liquid(final boolean liquid) {
        this.liquid = liquid;
    }

    @Override
    public void mapColor(final MapColor mapColor) {
        this.mapColor = mapColor;
    }

    @Override
    public void push(final EnumPushReaction push) {
        this.push = push;
    }

    @Override
    public void solid(final boolean solid) {
        this.solid = solid;
    }

    @Override
    public void replaceable(final boolean replaceable) {
        this.replaceable = replaceable;
    }

    @Override
    public void translucent(final boolean translucent) {
        this.translucent = translucent;
    }

    @Override
    public void toolRequired(final boolean toolRequired) {
        this.toolRequired = toolRequired;
    }

    @Override
    public Material build() {
        return null; // TODO(kashike)
    }
}
