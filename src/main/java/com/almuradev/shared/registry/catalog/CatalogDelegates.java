/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.registry.catalog;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class CatalogDelegates {

    private CatalogDelegates() {
    }

    public static Set<CatalogDelegate<ItemType>> items(final ConfigurationNode node) {
        if (node.getValue() instanceof List) {
            return node.getList(Types::asString).stream()
                    .map(CatalogDelegates::item)
                    .collect(Collectors.toSet());
        } else {
            return Collections.singleton(item(node.getString()));
        }
    }

    public static CatalogDelegate<ItemType> item(@Nullable String id) {
        if (id == null) {
            id = ItemStack.empty().getType().getId();
        }
        return new CatalogDelegate<>(ItemType.class, id);
    }
}
