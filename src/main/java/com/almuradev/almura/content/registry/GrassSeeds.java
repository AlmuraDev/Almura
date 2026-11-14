/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.def.ContentDefs.Range;
import com.almuradev.almura.content.def.ContentDefs.SeedDef;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Seeds from grass ({@code data/almura/loot_modifiers/grass_seeds.json}: short grass and ferns, not cut with shears), as 3.5's
 * {@code MixinBlockTallGrass} meant them: one break in eight picks one of the content's seeds at random, which drops at its
 * {@code grass} chance, as many as its amount. When vanilla's wheat seeds already dropped, nothing more drops.
 */
public final class GrassSeeds extends LootModifier {

    public static final MapCodec<GrassSeeds> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance,
        GrassSeeds::new));

    private record Seed(Item item, SeedDef def) {
    }

    private static final List<Seed> SEEDS = new ArrayList<>();

    public GrassSeeds(final Optional<Holder<LootItemCondition>> condition, final int priority) {
        super(condition, priority);
    }

    public static void register(final IEventBus modBus) {
        final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> serializers =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Almura.ID);
        serializers.register("grass_seeds", () -> CODEC);
        serializers.register(modBus);
    }

    static void add(final Item item, final SeedDef def) {
        if (def.grassChance() != null || def.grassAmountMax() > 0) {
            SEEDS.add(new Seed(item, def));
        }
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(final ObjectArrayList<ItemStack> generatedLoot, final LootContext context) {
        final RandomSource random = context.getRandom();
        if (SEEDS.isEmpty() || random.nextInt(8) != 0) {
            return generatedLoot;
        }
        // Don't double up with vanilla's seed
        if (generatedLoot.stream().anyMatch(stack -> stack.is(Items.WHEAT_SEEDS))) {
            return generatedLoot;
        }
        final Seed seed = SEEDS.get(random.nextInt(SEEDS.size()));
        final int amount = seed.def().grassAmountMin() + random.nextInt(Math.max(1, seed.def().grassAmountMax() - seed.def().grassAmountMin() + 1));
        final Range chance = seed.def().grassChance();
        if (amount > 0 && (chance == null || random.nextDouble() <= chance.random(random.nextDouble()) / 100)) {
            generatedLoot.add(new ItemStack(seed.item(), amount));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
