/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.predicate;

import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

public abstract class AbstractFunctionPredicateParser<I, O> implements ConfigurationNodeDeserializer<FunctionPredicate<I, O>> {
    protected final Function<I, O> function;

    protected AbstractFunctionPredicateParser(final Function<I, O> function) {
        this.function = function;
    }

    @Override
    public final Optional<FunctionPredicate<I, O>> deserialize(final ConfigurationNode config) {
        return Optional.ofNullable(this.deserializeOne(config));
    }

    @Nullable
    private FunctionPredicate<I, O> deserializeOne(final ConfigurationNode config) {
        if(config.getValue() == null) {
            return null;
        }
        return this.deserializeOne(config, config.getValue().getClass());
    }

    @Nullable
    private FunctionPredicate<I, O> deserializeOne(final ConfigurationNode config, final Class<?> type) {
        if (List.class.isAssignableFrom(type)) {
            return new AnyFunctionPredicate<>(this.function, this.deserializeList(config.getChildrenList()));
        } else if (Map.class.isAssignableFrom(type)) {
            return new AnyFunctionPredicate<>(this.function, this.deserializeMap(config.getChildrenMap().entrySet()));
        }
        return this.deserializeOne0(config, type);
    }

    @Nullable
    protected FunctionPredicate<I, O> deserializeOne0(final ConfigurationNode config, final Class<?> type) {
        return null;
    }

    private Optional<FunctionPredicate<I, O>> tryDeserialize(final ConfigurationNode config) {
        return Optional.ofNullable(this.deserializeOne(config, config.getValue().getClass()));
    }

    private List<FunctionPredicate<I, O>> deserializeList(final List<? extends ConfigurationNode> children) {
        final List<FunctionPredicate<I, O>> predicates = new ArrayList<>(children.size());
        for (final ConfigurationNode child : children) {
            predicates.add(this.deserializeOne(child));
        }
        return predicates;
    }

    private List<FunctionPredicate<I, O>> deserializeMap(final Set<? extends Map.Entry<Object, ? extends ConfigurationNode>> entries) {
        final List<FunctionPredicate<I, O>> predicates = new ArrayList<>(entries.size());
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : entries) {
            final String key = String.valueOf(entry.getKey());
            final Object value = entry.getValue().getValue();
            switch (key) {
                case AndFunctionPredicate.ID:
                    if (value instanceof Map) {
                        predicates.add(new AndFunctionPredicate<>(this.function, this.deserializeMap(entry.getValue().getChildrenMap().entrySet())));
                    } else if (value instanceof List) {
                        predicates.add(new AndFunctionPredicate<>(this.function, this.deserializeList(entry.getValue().getChildrenList())));
                    }
                    break;
                case AnyFunctionPredicate.ID:
                    this.tryDeserialize(entry.getValue()).ifPresent(predicates::add);
                    break;
                case NotFunctionPredicate.ID:
                    this.tryDeserialize(entry.getValue()).ifPresent(one -> predicates.add(new NotFunctionPredicate<>(this.function, one)));
                    break;
                default:
                    this.deserializeMap(predicates, key, entry.getValue());
            }
        }
        return predicates;
    }

    protected void deserializeMap(final List<FunctionPredicate<I, O>> predicates, final String key, final ConfigurationNode config) {
    }
}
