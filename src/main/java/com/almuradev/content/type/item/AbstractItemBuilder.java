/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.mixin.iface.IMixinLazyItemGroup;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.OptionalInt;

public abstract class AbstractItemBuilder<C extends ContentItem> extends ContentBuilder.Impl<C> implements ContentItem.Builder<C> {
    private OptionalInt durability = OptionalInt.empty();
    private Delegate<ItemGroup> itemGroup;
    private OptionalInt maxStackSize = OptionalInt.empty();

    @Override
    public void durability(final int durability) {
        this.durability = OptionalInt.of(durability);
    }

    @Override
    public void itemGroup(final Delegate<ItemGroup> itemGroup) {
        this.itemGroup = itemGroup;
    }

    @Override
    public void maxStackSize(final int maxStackSize) {
        this.maxStackSize = OptionalInt.of(maxStackSize);
    }

    @Override
    public void fill(final IForgeRegistryEntry.Impl entry) {
        super.fill(entry);
        ((Item) entry).setUnlocalizedName(this.string(StringType.TRANSLATION));
        ((IMixinLazyItemGroup) entry).itemGroup(this.itemGroup);
        this.durability.ifPresent(((Item) entry)::setMaxDamage);
        this.maxStackSize.ifPresent(((Item) entry)::setMaxStackSize);
    }
}
