/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply;

import com.almuradev.content.component.apply.impl.ApplyExhaustion;
import com.almuradev.content.component.apply.impl.ReduceDurability;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ApplyParserImpl implements ApplyParser {

    private final Map<String, ConfigurationNodeDeserializer<? extends Apply>> parsers = new HashMap<>();

    @Inject
    public ApplyParserImpl() {
        this.parsers.put("apply_exhaustion", ApplyExhaustion.PARSER);
        this.parsers.put("reduce_durability", ReduceDurability.PARSER);
    }

    @Override
    public List<Apply> parse(final ConfigurationNode config) {
        if (config.isVirtual()) {
            return Collections.emptyList();
        }
        return config.getChildrenMap().entrySet().stream()
                .map(
                        entry -> Optional.ofNullable(this.parsers.get(String.valueOf(entry.getKey())))
                                    .map(parser -> parser.deserialize(entry.getValue(), null))
                                    .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
