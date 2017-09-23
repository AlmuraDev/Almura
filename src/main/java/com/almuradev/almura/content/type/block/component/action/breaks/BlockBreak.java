/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.component.action.breaks;

import com.almuradev.almura.content.type.block.component.action.Action;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.Drop;
import com.almuradev.shared.registry.catalog.CatalogDelegate;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.item.ItemType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class BlockBreak {

    private final Set<CatalogDelegate<ItemType>> with;
    private final List<Action> actions;
    private final List<Drop> drops;

    BlockBreak(final Set<CatalogDelegate<ItemType>> with, final List<Action> actions, final List<Drop> drops) {
        this.with = ImmutableSet.copyOf(with);
        this.actions = ImmutableList.copyOf(actions);
        this.drops = ImmutableList.copyOf(drops);
    }

    public boolean accepts(ItemType type) {
        return this.with.stream().anyMatch(input -> input.test(type));
    }

    public Collection<Action> getActions() {
        return this.actions;
    }

    public Collection<Drop> getDrops() {
        return this.drops;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("with", this.with)
                .add("actions", this.actions)
                .add("drops", this.drops)
                .toString();
    }
}
