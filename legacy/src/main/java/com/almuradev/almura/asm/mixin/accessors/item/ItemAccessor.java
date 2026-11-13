/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Item.class)
public interface ItemAccessor {
    // public net.minecraft.item.Item field_77701_a # tabToDisplayOn
    @Accessor("tabToDisplayOn") CreativeTabs accessor$getTabToDisplayOn();
    @Accessor("tabToDisplayOn") void accessor$setTabToDisplayOn(CreativeTabs tabs);
}
