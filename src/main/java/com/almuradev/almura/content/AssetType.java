/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content;

import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.block.rotatable.HorizontalType;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.item.BuildableItemType;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.registry.BuildableCatalogType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public enum AssetType {
    ITEMGROUP(ItemGroup.Builder.class, CreativeTabs.class),
    BLOCK(BuildableBlockType.Builder.class, Block.class),
    HORIZONTAL(HorizontalType.Builder.class, BlockHorizontal.class),
    ITEM(BuildableItemType.Builder.class, Item.class),
    SOUNDGROUP(BlockSoundGroup.Builder.class, SoundType.class);

    private final Class<? extends BuildableCatalogType.Builder> builderClass;
    private final Class minecraftParentClass;

    AssetType(Class<? extends BuildableCatalogType.Builder> builderClass, Class minecraftParentClass) {
        this.builderClass = builderClass;
        this.minecraftParentClass = minecraftParentClass;
    }

    public Class<? extends BuildableCatalogType.Builder> getBuilderClass() {
        return this.builderClass;
    }

    public Class getMinecraftParentClass() {
        return this.minecraftParentClass;
    }
}
