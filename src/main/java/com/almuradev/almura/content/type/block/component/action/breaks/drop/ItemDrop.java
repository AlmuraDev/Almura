/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.breaks.drop;

import com.almuradev.shared.registry.catalog.CatalogDelegate;
import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ItemDrop extends Drop {

    private final List<CatalogDelegate<ItemType>> items;
    private final List<ItemStackSnapshot> drops = new ArrayList<>();

    public ItemDrop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance, final List<CatalogDelegate<ItemType>> items) {
        super(amount, bonusAmount, bonusChance);
        this.items = items;
    }

    public Collection<ItemStackSnapshot> getDrops() {
        if (this.drops.isEmpty()) {
            for (final CatalogDelegate<ItemType> item : this.items) {
                this.drops.add(((ItemStack) (Object) new net.minecraft.item.ItemStack((Item) item.get())).createSnapshot());
            }
        }
        return Collections.unmodifiableList(this.drops);
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("items", this.items);
    }
}
