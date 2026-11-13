/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ItemFinder<T> {
  public final String id;
  public final String translationKey;
  private final NonNullList<ItemStack> itemCache;
  private final Function<T, List<ItemStack>> function;
  private final boolean cacheItems;

  public ItemFinder(final String id, final String translationKey, final Function<T, List<ItemStack>> function, final boolean cacheItems) {
    this.id = id;
    this.translationKey = translationKey;
    this.itemCache = NonNullList.create();
    this.function = function;
    this.cacheItems = cacheItems;
  }

  public List<ItemStack> getItems(final T object) {

    if (this.cacheItems) {

      // If the cache is empty, populate it
      if (this.itemCache.isEmpty()) {
        this.itemCache.addAll(this.function.apply(object));
      }

      return Collections.unmodifiableList(this.itemCache);
    }

    // Clear to ensure it never stores a cache
    this.itemCache.clear();

    return Collections.unmodifiableList(this.function.apply(object));
  }
}