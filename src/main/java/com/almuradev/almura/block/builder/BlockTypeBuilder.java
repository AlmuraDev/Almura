/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder;

import com.almuradev.almura.Almura;
import com.almuradev.almura.api.block.BuildableBlockType;
import com.almuradev.almura.block.GenericBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class BlockTypeBuilder extends AbstractBlockTypeBuilder<BuildableBlockType, BlockTypeBuilder> {

    @Override
    public BuildableBlockType build(String id) {
        final GenericBlock block = new GenericBlock(this.material, this.mapColor);

        if (this.tabs != null) {
            block.setCreativeTab(tabs);
        }

        block.setRegistryName(Almura.PLUGIN_ID, id);

        return (BuildableBlockType) (Object) GameRegistry.register(block);
    }
}
