/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.content.type.block.state.BlockStateDefinition;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.material.MaterialType;
import org.spongepowered.api.block.BlockType;

import java.util.Map;
import java.util.Optional;

public interface BuildableBlockType extends MaterialType, BlockType {

    interface Builder<BLOCK extends BuildableBlockType, BUILDER extends Builder<BLOCK, BUILDER>> extends MaterialType.Builder<BLOCK, BUILDER> {

        Map<String, BlockStateDefinitionBuilder<?>> identifiedStates();

        default BlockStateDefinition onlyState() {
            checkState(!this.identifiedStates().isEmpty(), "block has no states");
            checkState(this.identifiedStates().size() == 1, "block has more than one state");
            return checkNotNull(this.identifiedStates().get(BlockStateDefinition.DEFAULT), "%s state", BlockStateDefinition.DEFAULT).build();
        }

        default <B extends BlockStateDefinitionBuilder<?>> Optional<B> findState(final String id) {
            return Optional.ofNullable((B) this.identifiedStates().get(id));
        }

        BUILDER putState(final BlockStateDefinitionBuilder<?> builder);

        @Override
        BLOCK build(String id);
    }
}
