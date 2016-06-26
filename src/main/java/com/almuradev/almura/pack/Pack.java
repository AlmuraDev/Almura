/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.pack.block.PackBlockObject;
import com.almuradev.almura.pack.item.PackItemObject;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.ResettableBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Pack implements CatalogType {

    public static Builder builder() {
        return new Builder();
    }

    private final String id, name;
    private final Map<String, PackObject> objectsById;

    protected Pack(String id, String name, Map<String, PackObject> objectsById) {
        this.id = id;
        this.name = name;
        this.objectsById = objectsById;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Set<PackBlockObject> getBlocks() {
        return this.objectsById.values().stream().filter(en -> en instanceof PackBlockObject).map(en -> (PackBlockObject) en).collect(Collectors
                .toSet());
    }

    public Set<PackItemObject> getItems() {
        return this.objectsById.values().stream().filter(en -> en instanceof PackItemObject).map(en -> (PackItemObject) en).collect(Collectors
                .toSet());
    }

    public Set<PackObject> getObjects() {
        return this.objectsById.values().stream().collect(Collectors.toSet());
    }

    public static final class Builder implements ResettableBuilder<Pack, Builder> {
        private final Map<String, PackObject> objectsById = new LinkedHashMap<>();

        public Builder object(PackObject object) {
            checkNotNull(object);
            checkArgument(!this.objectsById.containsValue(object), "Attempt to double add pack object to builder!");
            Sponge.getRegistry().getType(object.getClass(), object.getId()).ifPresent(obj -> {
                throw new IllegalArgumentException("Attempt to add [" + obj + "], an already registered object, to a Pack builder! When building a "
                        + "Pack, the object should not be registered yet!");
            });
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
