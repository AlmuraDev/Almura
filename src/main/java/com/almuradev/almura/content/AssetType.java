/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content;

import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.block.type.horizontal.HorizontalType;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.type.BuildableItemType;
import com.almuradev.almura.registry.BuildableCatalogType;

public enum AssetType {
    BLOCK("block", BuildableBlockType.Builder.class),
    HORIZONTAL_BLOCK("horizontal", HorizontalType.Builder.class),
    BLOCK_SOUNDGROUP("soundgroup", BlockSoundGroup.Builder.class),
    ITEM("item", BuildableItemType.Builder.class),
    ITEMGROUP("itemgroup", ItemGroup.Builder.class);

    private final String extension;
    private final Class<? extends BuildableCatalogType.Builder> builderClass;

    AssetType(final String extension, final Class<? extends BuildableCatalogType.Builder> builderClass) {
        this.extension = extension;
        this.builderClass = builderClass;
    }

    public String getExtension() {
        return this.extension;
    }

    public Class<? extends BuildableCatalogType.Builder> getBuilderClass() {
        return this.builderClass;
    }
}
