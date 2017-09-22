/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateMaterialAttributes;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.material.AbstractMaterialTypeBuilder;
import net.minecraft.block.Block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked"})
public abstract class AbstractBlockTypeBuilder<BLOCK extends BuildableBlockType, BUILDER extends AbstractBlockTypeBuilder<BLOCK, BUILDER>>
        extends AbstractMaterialTypeBuilder<BLOCK, BUILDER> implements BuildableBlockType.Builder<BLOCK, BUILDER> {

    protected final Map<String, BlockStateDefinitionBuilder<?>> states = new HashMap<>();

    protected <X extends BlockStateDefinitionBuilder<?>> Collection<X> castedStates() {
        return (Collection<X>) this.states.values();
    }

    @Override
    public Map<String, BlockStateDefinitionBuilder<?>> identifiedStates() {
        return this.states;
    }

    @Override
    public BUILDER putState(final BlockStateDefinitionBuilder<?> builder) {
        this.states.put(builder.id(), builder);
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
}
