/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.item;

import com.almuradev.almura.content.item.BuildableItemType;
import com.almuradev.almura.content.item.group.ItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

// Makes all items BuildableItemTypes (so they can be used in Almura's framework)
@Mixin(value = Item.class, priority = 999)
public abstract class MixinItem implements BuildableItemType {

    @Shadow public CreativeTabs tabToDisplayOn;

    @Override
    public Optional<ItemGroup> getItemGroup() {
        return Optional.ofNullable((ItemGroup) (Object) this.tabToDisplayOn);
    }
}
