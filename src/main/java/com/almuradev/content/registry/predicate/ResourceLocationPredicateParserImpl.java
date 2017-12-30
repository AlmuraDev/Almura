/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.registry.predicate;

import com.almuradev.almura.shared.registry.ResourceLocations;
import com.almuradev.content.component.predicate.AbstractFunctionPredicateParser;
import com.almuradev.content.component.predicate.FunctionPredicate;
import net.minecraft.util.ResourceLocation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

final class ResourceLocationPredicateParserImpl<T> extends AbstractFunctionPredicateParser<T, ResourceLocation> implements ResourceLocationPredicateParser<T> {

    ResourceLocationPredicateParserImpl(final Function<T, ResourceLocation> function) {
        super(function);
    }

    @Nullable
    @Override
    protected FunctionPredicate<T, ResourceLocation> deserializeOne0(final ConfigurationNode config, final Class<?> type) {
        if (String.class.isAssignableFrom(type)) {
            return this.deserializeString(config.getString());
        }
        return super.deserializeOne0(config, type);
    }

    // Zidane is going to be the death of me. ;_;
    private FunctionPredicate<T, ResourceLocation> deserializeString(final String string) {
        if (string.indexOf(ResourceLocations.NAMESPACE_SEPARATOR) != ResourceLocations.NOT_FOUND) {
            return new SingleResourceLocationPredicate<>(this.function, ResourceLocations.createNamespaced(string));
        }
        return new InResourceLocationPredicate<>(this.function, Collections.singletonList(string));
    }

    @Override
    protected void deserializeMap(final List<FunctionPredicate<T, ResourceLocation>> predicates, final String key, final ConfigurationNode config) {
        switch (key) {
            case InResourceLocationPredicate.ID:
                predicates.add(new InResourceLocationPredicate<>(this.function, config.getList(Types::asString)));
                break;
            default:
                throw new IllegalArgumentException("Could not find deserialization method for '" + key + "'");
        }
    }
}
