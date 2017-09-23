/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.asm.mixin.item;

import com.almuradev.content.asm.iface.IMixinLazyItemGroup;
import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.item.ContentItemType;
import com.almuradev.content.type.item.type.normal.NormalItemImpl;
import com.almuradev.content.type.item.type.seed.SeedItemImpl;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin({
    NormalItemImpl.class,
    SeedItemImpl.class
})
public abstract class MixinContentItem extends MixinItem implements ContentItemType, IMixinLazyItemGroup {

    @Nullable private Delegate<ItemGroup> lazyItemGroup;

    @Override
    public Optional<ItemGroup> itemGroup() {
        if (this.tabToDisplayOn != null) {
            return Optional.of((ItemGroup) this.tabToDisplayOn);
        }
        if (this.lazyItemGroup == null) {
            throw new IllegalStateException("Content item with a null item group");
        }
        this.tabToDisplayOn = (CreativeTabs) this.lazyItemGroup.get();
        return Optional.ofNullable((ItemGroup) this.tabToDisplayOn);
    }

    @Override
    public void itemGroup(final Delegate<ItemGroup> group) {
        this.lazyItemGroup = group;
    }

    @Nullable
    @Override
    public CreativeTabs getCreativeTab() {
        return (CreativeTabs) this.itemGroup().orElse(null);
    }
}
