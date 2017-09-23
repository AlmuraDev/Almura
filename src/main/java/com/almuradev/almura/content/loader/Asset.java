/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.block.type.horizontal.HorizontalType;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.type.BuildableItemType;
import com.almuradev.shared.registry.catalog.BuildableCatalogType;
import ninja.leaping.configurate.ConfigurationNode;

import java.nio.file.Path;

public final class Asset {

    private final String name;
    private final Type type;
    private final Path path;
    private final ConfigurationNode node;

    Asset(String name, Type type, Path path, ConfigurationNode node) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.node = node;
    }

    public String getName() {
        return this.name;
    }

    public Type getType() {
        return this.type;
    }

    public Path getPath() {
        return this.path;
    }

    public ConfigurationNode getConfigurationNode() {
        return this.node;
    }

    public enum Type {
        BLOCK("block", BuildableBlockType.Builder.class),
        HORIZONTAL_BLOCK("horizontal", HorizontalType.Builder.class),
        BLOCK_SOUNDGROUP("soundgroup", BlockSoundGroup.Builder.class),
        ITEM("item", BuildableItemType.Builder.class),
        ITEMGROUP("itemgroup", ItemGroup.Builder.class);

        private final String extension;
        private final Class<? extends BuildableCatalogType.Builder> builderClass;

        Type(final String extension, final Class<? extends BuildableCatalogType.Builder> builderClass) {
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
}
