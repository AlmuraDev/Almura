/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.ItemGrouped;
import com.almuradev.content.type.itemgroup.mixin.iface.IMixinLazyItemGroup;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.api.util.PEBKACException;

import java.util.Optional;
import java.util.OptionalInt;

// This cannot extend ItemType.
public interface ContentItemType extends CatalogedContent, ItemGrouped {

    @Override
    default String getId() {
        throw new PEBKACException("api");
    }

    @Override
    default String getName() {
        throw new PEBKACException("api");
    }

    @Override
    default Optional<ItemGroup> itemGroup() {
        throw new PEBKACException("mixin");
    }

    interface Builder<C extends ContentItemType> extends ContentBuilder<C> {
        void durability(final int durability);

        void itemGroup(final Delegate<ItemGroup> itemGroup);

        void maxStackSize(final int maxStackSize);

        abstract class Impl<C extends ContentItemType> extends ContentBuilder.Impl<C> implements Builder<C> {
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
    }

    interface Tier extends CatalogedContent {
        // TODO(kashike): support for custom tiers in the future
    }
}
