/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder.rotable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.almuradev.almura.api.block.rotable.RotableBlockType;
import com.almuradev.almura.block.GenericRotable;
import com.almuradev.almura.block.builder.AbstractBlockTypeBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;

@SuppressWarnings("unchecked")
public abstract class AbstractRotableTypeBuilder<ROTABLE extends RotableBlockType, BUILDER extends AbstractRotableTypeBuilder<ROTABLE, BUILDER>>
        extends AbstractBlockTypeBuilder<ROTABLE, BUILDER> implements RotableBlockType.Builder<ROTABLE, BUILDER> {

    public static final class BuilderImpl extends AbstractRotableTypeBuilder<RotableBlockType, BuilderImpl> {

        @Override
        public RotableBlockType build(String id) {
            checkNotNull(id);

            return (RotableBlockType) (Object) GameRegistry.register(new GenericRotable(Almura.PLUGIN_ID, id, this));
        }
    }
}
