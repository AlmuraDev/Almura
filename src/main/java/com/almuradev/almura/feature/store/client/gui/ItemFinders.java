/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client.gui;

import com.almuradev.almura.feature.store.Store;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.stream.Collectors;

public class ItemFinders {
  private static final String translationKeyBase = "almura.feature.store.itemfinder.";

  static final ItemFinder STORE_ITEM_FINDER = new ItemFinder<Store>("store", translationKeyBase + "store", (store) -> {
    final NonNullList<ItemStack> items = NonNullList.create();
    items.addAll(store.getBuyingItems()
      .stream()
      .map(i -> {
        final ItemStack copyStack = i.asRealStack().copy();
        copyStack.setCount(1);
        return copyStack;
      })
      .collect(Collectors.toList()));
    items.addAll(store.getSellingItems()
      .stream()
      .map(i -> {
        final ItemStack copyStack = i.asRealStack().copy();
        copyStack.setCount(1);
        return copyStack;
      })
      .filter(i1 -> items.stream().noneMatch(i2 -> ItemStack.areItemStacksEqual(i1, i2)))
      .collect(Collectors.toList()));

    return items;
  }, false);

  static final ItemFinder INVENTORY_ITEM_FINDER = new ItemFinder<EntityPlayer>("inventory", translationKeyBase + "inventory",
    (player) -> player.inventory.mainInventory
      .stream()
      .filter(i -> !i.isEmpty())
      .collect(Collectors.toList()), false);
}
