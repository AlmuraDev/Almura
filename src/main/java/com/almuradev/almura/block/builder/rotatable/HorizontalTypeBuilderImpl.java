/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder.rotatable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Constants;
import com.almuradev.almura.block.builder.AbstractBlockTypeBuilder;
import com.almuradev.almura.block.rotatable.HorizontalType;
import com.almuradev.almura.block.impl.rotatable.GenericHorizontal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class HorizontalTypeBuilderImpl extends AbstractBlockTypeBuilder<HorizontalType, HorizontalTypeBuilderImpl> implements HorizontalType
        .Builder<HorizontalType, HorizontalTypeBuilderImpl> {

    @Override
    public HorizontalType build(String id) {
        checkNotNull(id);

        final GenericHorizontal block = GameRegistry.register(new GenericHorizontal(Constants.Plugin.ID, id, this));

        final ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(Constants.Plugin.ID, id);

        ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(Constants.Plugin.ID + ":" + id, "facing=south"));

        // TODO Make this configurable and make Almura GenericItemBlock
        GameRegistry.register(itemBlock);

        return (HorizontalType) (Object) block;
    }
}
