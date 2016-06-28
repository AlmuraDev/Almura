/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder.rotable;

import com.almuradev.almura.Almura;
import com.almuradev.almura.api.block.rotable.RotableBlockType;
import com.almuradev.almura.block.GenericRotable;
import com.almuradev.almura.mixin.interfaces.IMixinRotableBlockType;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class RotableTypeBuilder extends AbstractRotableTypeBuilder<RotableBlockType, RotableTypeBuilder> {

    @Override
    public RotableBlockType build(String id) {
        final GenericRotable block = new GenericRotable(this.material);

        ((IMixinRotableBlockType) (Object) block).setMapColor(this.mapColor);

        if (this.tabs != null) {
            block.setCreativeTab(tabs);
        }

        block.setRegistryName(Almura.PLUGIN_ID, id);

        return (RotableBlockType) (Object) GameRegistry.register(block);
    }
}
