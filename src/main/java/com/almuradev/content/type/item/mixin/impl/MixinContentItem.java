/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.mixin.impl;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.item.ContentItem;
import com.almuradev.content.type.item.type.food.FoodItemImpl;
import com.almuradev.content.type.item.type.normal.NormalItemImpl;
import com.almuradev.content.type.item.type.seed.SeedItemImpl;
import com.almuradev.content.type.item.type.slab.SlabItemImpl;
import com.almuradev.content.type.item.type.tool.type.hoe.HoeToolItemImpl;
import com.almuradev.content.type.item.type.tool.type.pickaxe.PickaxeToolItemImpl;
import com.almuradev.content.type.item.type.tool.type.shovel.ShovelToolItemImpl;
import com.almuradev.content.type.item.type.tool.type.sickle.SickleToolItemImpl;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.mixin.iface.IMixinLazyItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin({
    FoodItemImpl.class,
    HoeToolItemImpl.class,
    NormalItemImpl.class,
    PickaxeToolItemImpl.class,
    SeedItemImpl.class,
    ShovelToolItemImpl.class,
    SlabItemImpl.class,
    SickleToolItemImpl.class
})
public abstract class MixinContentItem extends MixinItem implements ContentItem, IMixinLazyItemGroup {
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
