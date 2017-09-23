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

import java.util.function.Function;

public interface ResourceLocationPredicateParser<T> extends ConfigurationNodeDeserializer<FunctionPredicate<T, ResourceLocation>> {

    static <T> ResourceLocationPredicateParser<T> of(final Function<T, ResourceLocation> function) {
        return new ResourceLocationPredicateParserImpl<>(function);
    }
}
