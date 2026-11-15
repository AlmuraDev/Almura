/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * What a cache holds: one item (with its components) and how many, far more than a stack. Kept as the item and its count apart, as an
 * item stack is saved with at most 99.
 */
public record CacheStack(ItemResource resource, int amount) {

    public static final CacheStack EMPTY = new CacheStack(ItemResource.EMPTY, 0);

    public static final Codec<CacheStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemResource.OPTIONAL_CODEC.optionalFieldOf("item", ItemResource.EMPTY).forGetter(CacheStack::resource),
        Codec.INT.optionalFieldOf("count", 0).forGetter(CacheStack::amount)
    ).apply(instance, CacheStack::of));

    /**
     * @return the stack, or {@link #EMPTY} for no item or none of it
     */
    public static CacheStack of(final ItemResource resource, final int amount) {
        return resource.isEmpty() || amount <= 0 ? EMPTY : new CacheStack(resource, amount);
    }

    public boolean isEmpty() {
        return this.resource.isEmpty() || this.amount <= 0;
    }
}
