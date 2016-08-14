/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.ResettableBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Pack implements CatalogType {

    private final String id, name;
    private final Map<String, CatalogType> objectsById;

    protected Pack(String id, String name, Map<String, CatalogType> objectsById) {
        this.id = id;
        this.name = name;
        this.objectsById = objectsById;
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
        return this.objectsById.values().stream().filter(en -> en instanceof BlockType).map(en -> (BlockType) en).collect(Collectors
                .toSet());
    }

    public Set<ItemType> getItems() {
        return this.objectsById.values().stream().filter(en -> en instanceof ItemType).map(en -> (ItemType) en).collect(Collectors
                .toSet());
    }

    public Set<CatalogType> getObjects() {
        return this.objectsById.values().stream().collect(Collectors.toSet());
    }

    public static final class Builder implements ResettableBuilder<Pack, Builder> {

        private final Map<String, CatalogType> objectsById = new LinkedHashMap<>();

        public Builder object(CatalogType object) {
            checkNotNull(object);
            checkArgument(!this.objectsById.containsValue(object), "Attempt to double add catalog type to builder!");
            this.objectsById.put(object.getId(), object);
            return this;
        }

        @Override
        public Builder from(Pack value) {
            throw new UnsupportedOperationException("Building a Pack from a Pack is unsupported!");
        }

        @Override
        public Builder reset() {
            this.objectsById.clear();
            return this;
        }

        public Pack build(String id, String name) {
            checkNotNull(id);
            checkNotNull(name);
            return new Pack(id, name, objectsById);
        }
    }
}
