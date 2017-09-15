/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.ResettableBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A "Pack" acts as a {@link CatalogType} whose goal is to be a container of catalog types.
 */
public class Pack implements CatalogType {

    private final String id;
    private final String name;
    private final Map<String, CatalogType> catalogs;

    Pack(String id, String name, Map<String, CatalogType> catalogs) {
        this.id = id;
        this.name = name;
        this.catalogs = catalogs;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Set<BlockType> getBlocks() {
        return this.catalogs.values().stream()
                .filter(type -> type instanceof BlockType)
                .map(type -> (BlockType) type)
                .collect(Collectors.toSet());
    }

    public Set<ItemType> getItems() {
        return this.catalogs.values().stream()
                .filter(type -> type instanceof ItemType)
                .map(type -> (ItemType) type)
                .collect(Collectors.toSet());
    }

    public void add(CatalogType object) {
        checkNotNull(object);
        checkArgument(!this.catalogs.containsValue(object), "Attempt to double add catalog type to pack!");
    }

    public Set<CatalogType> getChildren() {
        return this.catalogs.values().stream().collect(Collectors.toSet());
    }

    public static final class Builder implements ResettableBuilder<Pack, Builder> {

        private final Map<String, CatalogType> catalogs = new LinkedHashMap<>();

        public Builder object(CatalogType object) {
            checkNotNull(object);
            checkArgument(!this.catalogs.containsValue(object), "Attempt to double add catalog type to builder!");

            this.catalogs.put(object.getId(), object);
            return this;
        }

        @Override
        public Builder from(Pack value) {
            throw new UnsupportedOperationException("Building a Pack from a Pack is unsupported!");
        }

        @Override
        public Builder reset() {
            this.catalogs.clear();
            return this;
        }

        public Pack build(String id, String name) {
            checkNotNull(id, "id");
            checkState(!id.isEmpty(), "Id cannot be empty!");
            checkNotNull(name, "name");
            checkState(!name.isEmpty(), "Name cannot be empty!");

            return new Pack(id, name, this.catalogs);
        }
    }
}
