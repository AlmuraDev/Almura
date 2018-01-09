/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdestroy;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.type.item.definition.ItemDefinition;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.ItemType;

import java.util.List;

public final class BlockDestroyActionEntry implements BlockDestroyAction.Entry {

    private final List<Apply> apply;
    private final List<? extends Drop> drop;
    private final List<ItemDefinition> with;
    private final boolean emptyWith;

    private BlockDestroyActionEntry(final Builder builder) {
        this.apply = builder.apply;
        this.drop = builder.drop;
        this.with = builder.with;
        this.emptyWith = this.with.isEmpty();
    }

    @Override
    public boolean test(final ItemStack stack) {
        return this.emptyWith || this.with.stream().anyMatch(item -> item.test(stack));
    }

    @Override
    public boolean test(final ItemType type) {
        return this.emptyWith || this.with.stream().anyMatch(item -> item.test(type));
    }

    @Override
    public List<? extends Apply> apply() {
        return this.apply;
    }

    @Override
    public List<? extends Drop> drops() {
        return this.drop;
    }

    public static class Builder implements BlockDestroyAction.Entry.Builder {

        private List<Apply> apply;
        private List<? extends Drop> drop;
        private List<ItemDefinition> with;

        @Override
        public void apply(final List<Apply> apply) {
            this.apply = apply;
        }

        @Override
        public void drop(final List<? extends Drop> drop) {
            this.drop = drop;
        }

        @Override
        public void with(final List<ItemDefinition> with) {
            this.with = with;
        }

        @Override
        public BlockDestroyAction.Entry build() {
            return new BlockDestroyActionEntry(this);
        }
    }
}
