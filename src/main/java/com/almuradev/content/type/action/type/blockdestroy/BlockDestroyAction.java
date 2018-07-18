/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdestroy;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.type.action.ActionContentType;
import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.type.item.definition.ItemDefinition;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.ItemType;

import java.util.List;

public interface BlockDestroyAction extends ActionContentType {
    List<Entry> entries();

    interface Builder extends ActionContentType.Builder<BlockDestroyAction> {
        Entry.Builder entry(final int index);
    }

    interface Entry {
        boolean test(final ItemStack stack);

        boolean test(final ItemType type);

        List<? extends Apply> apply();

        List<? extends Drop> drops();

        interface Builder {
            void apply(final List<Apply> apply);

            void drop(final List<? extends Drop> drop);

            void with(final List<ItemDefinition> with);

            Entry build();
        }
    }
}
