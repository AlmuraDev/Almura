/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateMaterialAttributes;
import com.almuradev.almura.content.type.block.state.BlockStateDefinition;
import com.almuradev.almura.content.type.block.type.generic.GenericBlock;
import com.almuradev.almura.content.type.material.AbstractMaterialTypeBuilder;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked"})
public abstract class AbstractBlockTypeBuilder<BLOCK extends BuildableBlockType, BUILDER extends AbstractBlockTypeBuilder<BLOCK, BUILDER>>
        extends AbstractMaterialTypeBuilder<BLOCK, BUILDER> implements BuildableBlockType.Builder<BLOCK, BUILDER> {

    private final List<BlockStateDefinition> states = new ArrayList<>();

    @Override
    public List<BlockStateDefinition> states() {
        return this.states;
    }

    @Override
    public BUILDER state(final BlockStateDefinition state) {
        this.states.add(state);
        return (BUILDER) this;
    }

    @Override
    public BLOCK build(String id) {
        checkNotNull(id, "id");
        checkState(!id.isEmpty(), "Id cannot be empty!");

        final Block block = this.createBlock((BUILDER) this);
        block.setRegistryName(id.split(":")[1]);
        block.setUnlocalizedName(id.replace(":", ".").replace("/", "."));

        ((IMixinDelegateMaterialAttributes) block).setItemGroupDelegate(this.itemGroup());

        return (BLOCK) (Object) block;
    }

    protected abstract Block createBlock(BUILDER builder);

    public static final class BuilderImpl extends AbstractBlockTypeBuilder<BuildableBlockType, BuilderImpl> {

        @Override
        protected Block createBlock(BuilderImpl builder) {
            return new GenericBlock(builder);
        }
    }
}
