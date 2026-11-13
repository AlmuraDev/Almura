/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdecay;

import com.almuradev.content.type.action.ActionContentType;
import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;

public interface BlockDecayAction extends ActionContentType {
    List<Entry> entries();

    interface Builder extends ActionContentType.Builder<BlockDecayAction> {
        Entry.Builder entry(final int index);
    }

    interface Entry {
        List<DoubleRangeFunctionPredicatePair<Biome>> chances();

        List<? extends Drop> drops();

        boolean test(final Biome biome, final Random random);

        interface Builder {
            void chance(final List<DoubleRangeFunctionPredicatePair<Biome>> chances);

            void drop(final List<? extends Drop> drop);

            Entry build();
        }
    }
}
