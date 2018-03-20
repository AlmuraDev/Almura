/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.type.blockdecay;

import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;

public final class BlockDecayActionEntry implements BlockDecayAction.Entry {
    private final List<DoubleRangeFunctionPredicatePair<Biome>> chances;
    private final List<? extends Drop> drops;

    private BlockDecayActionEntry(final Builder builder) {
        this.chances = builder.chances;
        this.drops = builder.drops;
    }

    @Override
    public List<DoubleRangeFunctionPredicatePair<Biome>> chances() {
        return this.chances;
    }

    @Override
    public List<? extends Drop> drops() {
        return this.drops;
    }

    @Override
    public boolean test(Biome biome, Random random) {
        final DoubleRange chance = DoubleRangeFunctionPredicatePair.range(this.chances, biome);
        return chance != null && !(chance.max() <= 0) && random.nextDouble() <= (chance.random(random) / 100d);
    }

    public static class Builder implements BlockDecayAction.Entry.Builder {

        List<DoubleRangeFunctionPredicatePair<Biome>> chances;
        List<? extends Drop> drops;

        @Override
        public void chance(final List<DoubleRangeFunctionPredicatePair<Biome>> chances) {
            this.chances = chances;
        }

        @Override
        public void drop(final List<? extends Drop> drops) {
            this.drops = drops;
        }

        @Override
        public BlockDecayAction.Entry build() {
            return new BlockDecayActionEntry(this);
        }
    }
}
