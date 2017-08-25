/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.component.action.blockbreak.drop;

import com.almuradev.almura.content.loader.CatalogDelegate;
import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public final class ItemDrop extends Drop {

    private final List<CatalogDelegate<ItemType>> itemDelegates;
    private final List<ItemStackSnapshot> drops = new ArrayList<>();

    public ItemDrop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance, final List<CatalogDelegate<ItemType>> itemDelegates) {
        super(amount, bonusAmount, bonusChance);
        this.itemDelegates = itemDelegates;
    }

    public Collection<ItemStackSnapshot> getDrops() {
        if (this.drops.isEmpty()) {
            for (CatalogDelegate<ItemType> itemDelegate : this.itemDelegates) {
                this.drops.add(((org.spongepowered.api.item.inventory.ItemStack) (Object) new ItemStack((Item) itemDelegate.getCatalog()))
                        .createSnapshot());
            }
        }

        return Collections.unmodifiableList(this.drops);
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("itemDelegates", this.itemDelegates);
    }
}
