/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder.rotatable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.block.builder.AbstractBlockTypeBuilder;
import com.almuradev.almura.block.impl.rotatable.GenericHorizontal;
import com.almuradev.almura.block.rotatable.HorizontalType;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class HorizontalTypeBuilderImpl extends AbstractBlockTypeBuilder<HorizontalType, HorizontalTypeBuilderImpl> implements HorizontalType
        .Builder<HorizontalType, HorizontalTypeBuilderImpl> {

    @Override
    public HorizontalType build(String id) {
        checkNotNull(id);
        checkState(!id.isEmpty(), "Id cannot be empty!");

        final String[] idAndPath = id.split(":");
        final String modid = idAndPath[0];
        final String name = idAndPath[1];
        final Block block = GameRegistry.register(new GenericHorizontal(modid, name, this).setRegistryName(name));
        final Item item = GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));

        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(id, "inventory"));

        return (HorizontalType) (Object) block;
    }
}
