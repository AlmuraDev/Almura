/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.moc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Collection;
import java.util.Optional;

/**
 * What Mo' Creatures' animals drop, to fit Almura's recipes, as 3.5's: turkeys at least 4 raw turkey (none from chicks), boars at least 2
 * porkchops and 3 pork belly, ducks a raw duck, cod, bass and salmon only Almura's raw fish (not Mo' Creatures' own), bees honeycomb (its
 * only source: honey pots, mead, honey nut bread) and komodo dragons 2 lizard meat.
 *
 * <p>Creatures are matched by their entity id ({@code mocreatures:cod}), not their name, so name-tagged ones still get their drops and other
 * mobs can't get them by being named "Cod". Only registered with Mo' Creatures loaded, and it names Mo' Creatures by id alone, so there is no
 * dependency on it.</p>
 */
public final class MocDrops {

    static final String MOD_ID = "mocreatures";

    @SubscribeEvent
    public void onDrops(final LivingDropsEvent event) {
        final LivingEntity entity = event.getEntity();
        final Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (!MOD_ID.equals(id.getNamespace())) {
            return;
        }
        final Collection<ItemEntity> drops = event.getDrops();
        switch (id.getPath()) {
            case "turkey" -> {
                // Chicks drop nothing extra
                if (!entity.isBaby()) {
                    topUp(entity, drops, "mocreatures:turkeyraw", 4);
                }
            }
            case "boar" -> {
                topUp(entity, drops, "minecraft:porkchop", 2);
                add(entity, drops, "almura:normal/ingredient/porkbelly_raw", 3);
            }
            case "duck" -> add(entity, drops, "almura:normal/ingredient/duck_raw", 1);
            // Only Almura's fish: Mo' Creatures' own fish (and its eggs) are dropped
            case "cod" -> replace(entity, drops, "almura:normal/ingredient/cod_raw");
            case "bass" -> replace(entity, drops, "almura:normal/ingredient/bass_raw");
            case "salmon" -> replace(entity, drops, "almura:normal/ingredient/salmon_raw");
            case "bee" -> add(entity, drops, "almura:food/food/honeycomb", 1);
            case "komododragon" -> add(entity, drops, "almura:normal/ingredient/lizard", 2);
            default -> {
            }
        }
    }

    // Adds enough of the item for the drops to hold at least that many
    private static void topUp(final LivingEntity entity, final Collection<ItemEntity> drops, final String item, final int minimum) {
        final Optional<Item> found = item(item);
        if (found.isEmpty()) {
            return;
        }
        int have = 0;
        for (final ItemEntity drop : drops) {
            if (drop.getItem().is(found.get())) {
                have += drop.getItem().getCount();
            }
        }
        if (have < minimum) {
            drops.add(drop(entity, new ItemStack(found.get(), minimum - have)));
        }
    }

    private static void add(final LivingEntity entity, final Collection<ItemEntity> drops, final String item, final int count) {
        item(item).ifPresent(found -> drops.add(drop(entity, new ItemStack(found, count))));
    }

    private static void replace(final LivingEntity entity, final Collection<ItemEntity> drops, final String item) {
        item(item).ifPresent(found -> {
            drops.clear();
            drops.add(drop(entity, new ItemStack(found)));
        });
    }

    private static ItemEntity drop(final LivingEntity entity, final ItemStack stack) {
        final ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
        drop.setDefaultPickUpDelay();
        return drop;
    }

    private static Optional<Item> item(final String id) {
        final Identifier key = Identifier.tryParse(id);
        return key == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(key);
    }
}
