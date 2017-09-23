/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.asm.mixin.item;

import com.almuradev.content.type.item.ContentItemType;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = Item.class, priority = 999)
public abstract class MixinItem implements ContentItemType {

    @Shadow public CreativeTabs tabToDisplayOn;
    @Nullable @Shadow public abstract CreativeTabs getCreativeTab();

    @Override
    public Optional<ItemGroup> itemGroup() {
        return Optional.ofNullable((ItemGroup) this.tabToDisplayOn);
    }
}
