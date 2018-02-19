/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.registry.predicate;

import com.almuradev.content.component.predicate.FunctionPredicate;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import net.minecraft.util.ResourceLocation;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public interface ResourceLocationPredicateParser<T> extends ConfigurationNodeDeserializer<FunctionPredicate<T, ResourceLocation>> {
    static <T> ResourceLocationPredicateParser<T> of(final Function<T, ResourceLocation> function) {
        return new ResourceLocationPredicateParserImpl<>(function);
    }

    default FunctionPredicate<T, ResourceLocation> allowingSingleAlwaysTrueDefault(final AtomicBoolean foundDefault, final ConfigurationNode config, final String name) {
        return this.deserialize(config).orElseGet(() -> {
            if (!foundDefault.compareAndSet(false, true)) {
                throw new IllegalStateException("Cannot have more than one default " + name);
            }
            return this.alwaysTrue();
        });
    }

    FunctionPredicate<T, ResourceLocation> alwaysTrue();
}
