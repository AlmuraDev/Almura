/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.breaks;

import com.almuradev.almura.content.type.VariableAmounts;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.component.action.Action;
import com.almuradev.almura.content.type.block.component.action.breaks.action.ApplyExhaustionAction;
import com.almuradev.almura.content.type.block.component.action.breaks.action.ApplyItemDamageAction;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.Drop;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.ExperienceDrop;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.ItemDrop;
import com.almuradev.shared.config.ConfigurationNodeDeserializer;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
import com.google.common.collect.ImmutableMap;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.Types;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public enum BlockBreakSerializer implements ConfigurationNodeDeserializer<List<BlockBreak>> {
    INSTANCE;

    private final ConfigurationNode emptyNode = new SimpleConfigurationNode(null, null, ConfigurationOptions.defaults()) {
        @Override
        public boolean isVirtual() {
            return true;
        }
    };
    private final Map<String, ConfigurationNodeDeserializer<? extends Action>> apply = ImmutableMap.<String, ConfigurationNodeDeserializer<? extends Action>>builder()
            .put("exhaustion", ApplyExhaustionAction.SERIALIZER)
            .put("damage_item", ApplyItemDamageAction.SERIALIZER)
            .build();

    @Override
    public Optional<List<BlockBreak>> deserialize(final ConfigurationNode node) {
        final List<BlockBreak> breaks = new ArrayList<>(node.getChildrenList().size());
        for (final ConfigurationNode child : node.getChildrenList()) {
            this.deserializeSingle(child).ifPresent(breaks::add);
        }
        return Optional.of(breaks);
    }

    private Optional<BlockBreak> deserializeSingle(final ConfigurationNode node) {
        final Set<CatalogDelegate<ItemType>> with = this.deserializeWith(node.getNode(BlockConfig.State.Action.Break.WITH));
        final List<Action> apply = this.deserializeApply(node.getNode(BlockConfig.State.Action.Break.APPLY));
        final List<Drop> drops = this.deserializeDrops(node.getNode(BlockConfig.State.Action.Break.DROPS));
        return Optional.of(new BlockBreak(with, apply, drops));
    }

    private Set<CatalogDelegate<ItemType>> deserializeWith(final ConfigurationNode node) {
        if (node.getValue() instanceof List) {
            return node.getList(Types::asString).stream()
                    .map(this::itemType)
                    .collect(Collectors.toSet());
        } else {
            return Collections.singleton(this.itemType(node.getString()));
        }
    }

    private List<Action> deserializeApply(final ConfigurationNode node) {
        final List<Action> apply = new ArrayList<>();
        for (final ConfigurationNode child : node.getChildrenList()) {
            final Object value = child.getValue();
            if (value instanceof String) {
                this.deserializeApplyAction(String.valueOf(value), this.emptyNode).ifPresent(apply::add);
            } else {
                for (final Map.Entry<Object, ? extends ConfigurationNode> entry : child.getChildrenMap().entrySet()) {
                    this.deserializeApplyAction(String.valueOf(entry.getKey()), entry.getValue()).ifPresent(apply::add);
                }
            }
        }
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
            final String key = String.valueOf(entry.getKey());
            final ConfigurationNodeDeserializer<? extends Action> serializer = this.apply.get(key);
            if (serializer == null) {
                throw new IllegalArgumentException("Unknown serializer for '" + key + "'");
            }
            serializer.deserialize(entry.getValue()).ifPresent(apply::add);
        }
        return apply;
    }

    private Optional<? extends Action> deserializeApplyAction(final String key, final ConfigurationNode node) {
        final ConfigurationNodeDeserializer<? extends Action> serializer = this.apply.get(key);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown serializer for '" + key + "'");
        }
        return serializer.deserialize(node);
    }

    private List<Drop> deserializeDrops(final ConfigurationNode node) {
        final List<Drop> drops = new ArrayList<>();
        for (int i = 0; i < node.getChildrenList().size(); i++) {
            final ConfigurationNode dropsNodeEl = node.getChildrenList().get(i);
            final VariableAmount amount = VariableAmounts.deserialize(dropsNodeEl.getNode(VariableAmounts.Config.AMOUNT), VariableAmounts.FIXED_1);
            @Nullable final VariableAmount bonusAmount = VariableAmounts.deserialize(dropsNodeEl.getNode(VariableAmounts.Config.BONUS, VariableAmounts.Config.BONUS_AMOUNT)).orElse(null);
            @Nullable final VariableAmount bonusChance = VariableAmounts.deserialize(dropsNodeEl.getNode(VariableAmounts.Config.BONUS, VariableAmounts.Config.BONUS_CHANCE)).orElse(null);
            if (!dropsNodeEl.getNode(BlockConfig.State.Action.Break.Drop.ITEM).isVirtual()) {
                final List<CatalogDelegate<ItemType>> items = dropsNodeEl.getNode(BlockConfig.State.Action.Break.Drop.ITEM).getList(Types::asString).stream()
                        .map(this::itemType)
                        .collect(Collectors.toList());
                drops.add(new ItemDrop(amount, bonusAmount, bonusChance, items));
            }
            if (!dropsNodeEl.getNode(BlockConfig.State.Action.Break.Drop.EXPERIENCE).isVirtual()) {
                drops.add(new ExperienceDrop(amount, bonusAmount, bonusChance));
            }
        }
        return drops;
    }

    private CatalogDelegate<ItemType> itemType(String id) {
        if (id == null) {
            id = ItemStack.empty().getItem().getId();
        }
        return new CatalogDelegate<>(ItemType.class, id);
    }
}
