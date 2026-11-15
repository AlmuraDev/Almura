/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache;

import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.IntSupplier;

/**
 * A cache's one slot, as 3.5's {@code SingleSlotItemHandler}: one item, up to the cache's limit. Hoppers and pipes fill and empty it through
 * NeoForge's item capability, which minds the limit (3.5 needed a mixin for Forge's hoppers to).
 */
public final class CacheHandler extends StacksResourceHandler<CacheStack, ItemResource> {

    private final IntSupplier limit;
    private final Runnable changed;

    CacheHandler(final IntSupplier limit, final Runnable changed) {
        super(1, CacheStack.EMPTY, CacheStack.CODEC);
        this.limit = limit;
        this.changed = changed;
    }

    public CacheStack stack() {
        return this.stacks.getFirst();
    }

    public int limit() {
        return this.limit.getAsInt();
    }

    /**
     * Puts up to {@code amount} of the item in, now.
     *
     * @return how many went in
     */
    public int insertNow(final ItemResource resource, final int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            final int inserted = this.insert(0, resource, amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    /**
     * Takes up to {@code amount} of what it holds out, now.
     *
     * @return how many came out
     */
    public int extractNow(final int amount) {
        final CacheStack stack = this.stack();
        if (stack.isEmpty()) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            final int extracted = this.extract(0, stack.resource(), amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    /**
     * Holds exactly this (placed from an item that held it).
     */
    public void setNow(final CacheStack stack) {
        this.set(0, stack.isEmpty() ? ItemResource.EMPTY : stack.resource(), stack.isEmpty() ? 0 : stack.amount());
    }

    @Override
    protected ItemResource getResourceFrom(final CacheStack stack) {
        return stack.resource();
    }

    @Override
    protected int getAmountFrom(final CacheStack stack) {
        return stack.amount();
    }

    @Override
    protected CacheStack getStackFrom(final ItemResource resource, final int amount) {
        return CacheStack.of(resource, amount);
    }

    @Override
    protected CacheStack copyOf(final CacheStack stack) {
        // A record: nothing in it changes
        return stack;
    }

    @Override
    protected int getCapacity(final int index, final ItemResource resource) {
        return this.limit.getAsInt();
    }

    @Override
    protected void onContentsChanged(final int index, final CacheStack previous) {
        this.changed.run();
    }
}
