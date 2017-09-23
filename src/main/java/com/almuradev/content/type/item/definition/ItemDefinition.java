/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.definition;

import com.almuradev.almura.shared.registry.ResourceLocations;
import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.registry.delegate.CatalogDelegate;
import com.almuradev.content.type.action.component.drop.Droppable;
import com.almuradev.content.type.action.component.drop.ItemDrop;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public final class ItemDefinition implements Droppable {

    @Deprecated private static final int DEFAULT_META = 0;
    private static final int DEFAULT_QUANTITY = 1;
    public static final ConfigurationNodeDeserializer<ItemDefinition> PARSER = config -> {
        if (config.isVirtual()) {
            return Optional.empty();
        }

        // item_id:
        //   quantity: <amount>
        //   meta: <value>
        if (config.getValue() instanceof Map) {
            // This intentionally does not loop.
            for (final Map.Entry<Object, ? extends ConfigurationNode> itemNodeEntry : config.getChildrenMap().entrySet()) {
                final ConfigurationNode itemNode = itemNodeEntry.getValue();

                final Delegate<ItemType> item = CatalogDelegate.create(ItemType.class, ResourceLocations.requireNamespaced(String.valueOf(itemNodeEntry.getKey())));
                final int quantity = itemNode.getNode(ItemDefinitionConfig.QUANTITY).getInt(DEFAULT_QUANTITY);
                final int meta = itemNode.getNode(ItemDefinitionConfig.META).getInt(DEFAULT_META);
                return Optional.of(new ItemDefinition(item, quantity, meta));
            }
        } else if (config.getValue() instanceof String) {
            return Optional.of(new ItemDefinition(CatalogDelegate.create(ItemType.class, ResourceLocations.requireNamespaced(config)), DEFAULT_QUANTITY, DEFAULT_META));
        }

        final Delegate<ItemType> item = CatalogDelegate.create(ItemType.class, ResourceLocations.requireNamespaced(config.getNode(ItemDefinitionConfig.ITEM)));
        final int quantity = config.getNode(ItemDefinitionConfig.QUANTITY).getInt(DEFAULT_QUANTITY);
        final int meta = config.getNode(ItemDefinitionConfig.META).getInt(DEFAULT_META);
        return Optional.of(new ItemDefinition(item, quantity, meta));
    };
    private final Delegate<ItemType> item;
    private final int quantity;
    @Deprecated private final int meta;

    private ItemDefinition(final Delegate<ItemType> item, final int quantity, @Deprecated final int meta) {
        this.item = item;
        this.quantity = quantity;
        this.meta = meta;
    }

    public boolean test(final ItemStack stack) {
        return this.item.get().equals(stack.getItem());
    }

    public boolean test(final ItemType type) {
        return this.item.get().getType().equals(type);
    }

    public ItemStack create() {
        return new ItemStack((Item) this.item.get(), this.quantity, this.meta);
    }

    @Override
    public ItemDrop asDrop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance) {
        return new ItemDrop(amount, bonusAmount, bonusChance, this);
    }
}
