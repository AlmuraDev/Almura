/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal;

import com.almuradev.almura.Almura;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;

/**
 * Replaces drops of the given items with another item, each at a chance ({@code data/almura/loot_modifiers/animal/*.json}), as 3.5's
 * {@code ItemReturnHelper} changed loot: clams join the fish caught (15 in 115 of the fish), and a sunflower drops 16 sunflower seeds
 * instead of itself.
 */
public final class ReplaceDropModifier extends LootModifier {

    public static final MapCodec<ReplaceDropModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(instance.group(
        RegistryCodecs.holderSet(Registries.ITEM).fieldOf("replaces").forGetter(modifier -> modifier.replaces),
        Item.CODEC.fieldOf("with").forGetter(modifier -> modifier.with),
        Codec.INT.optionalFieldOf("count", 1).forGetter(modifier -> modifier.count),
        Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(modifier -> modifier.chance)
    )).apply(instance, ReplaceDropModifier::new));

    private final HolderSet<Item> replaces;
    private final Holder<Item> with;
    private final int count;
    private final double chance;

    public ReplaceDropModifier(final Optional<Holder<LootItemCondition>> condition, final int priority, final HolderSet<Item> replaces,
        final Holder<Item> with, final int count, final double chance) {
        super(condition, priority);
        this.replaces = replaces;
        this.with = with;
        this.count = count;
        this.chance = chance;
    }

    /**
     * Registers the modifier's type ({@code almura:replace_drop}). Called by the mod's setup with its event bus.
     */
    public static void register(final IEventBus modBus) {
        final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> serializers =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Almura.ID);
        serializers.register("replace_drop", () -> CODEC);
        serializers.register(modBus);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(final ObjectArrayList<ItemStack> generatedLoot, final LootContext context) {
        for (int i = 0; i < generatedLoot.size(); i++) {
            final ItemStack stack = generatedLoot.get(i);
            if (stack.is(this.replaces) && context.getRandom().nextDouble() < this.chance) {
                generatedLoot.set(i, new ItemStack(this.with, this.count));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
