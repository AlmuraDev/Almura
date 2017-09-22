/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.crop;

import com.almuradev.almura.content.type.block.type.AbstractBlockTypeBuilder;
import com.almuradev.almura.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.almura.content.type.block.type.crop.state.CropBlockStateDefinitionBuilder;
import net.minecraft.block.Block;

import java.util.Collection;

public final class CropBlockTypeBuilder extends AbstractBlockTypeBuilder<CropBlockType, CropBlockTypeBuilder> implements CropBlockType.Builder<CropBlockType, CropBlockTypeBuilder> {

    private int nsi;

    public int allocateId() {
        return this.nsi++;
    }

    // TODO improve
    public CropBlockStateDefinition[] states() {
        final Collection<CropBlockStateDefinitionBuilder> builders = this.castedStates();
        final CropBlockStateDefinition[] states = new CropBlockStateDefinition[builders.size()];
        for (CropBlockStateDefinitionBuilder builder : builders) {
            states[builder.id] = builder.build();
        }
        return states;
    }

    @Override
    protected Block createBlock(final CropBlockTypeBuilder builder) {
        return new CropBlock(builder);
    }
}
