/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.armor;

import com.almuradev.almura.Almura;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Almura's hand coded armor, as 3.5's {@code ArmorFeature}: the lighted helmet, a helmet with a spotlight that lights up the spot the wearer
 * is looking at, like a flashlight. The light is the client's alone ({@code client.HelmetLights}): every client lights up the players it
 * sees wearing one, and the server's light levels (mob spawning, crops) never change.
 * <p>
 * As on 3.5, it has an iron helmet's protection, durability and look, and no recipe (it's handed out).
 */
public final class LightedHelmet {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Almura.ID, "armor/lighted_helmet");
    /** The light level of the spot where the wearer is looking (a torch is 14). */
    public static final int SPOT_LIGHT_LEVEL = 13;
    /** The light level of the four outer rays that widen the beam. */
    public static final int BEAM_EDGE_LIGHT_LEVEL = 12;
    /** How far the outer rays fan out from where the wearer is looking (up, down, left and right), in degrees. */
    public static final float BEAM_SPREAD_DEGREES = 15.0F;
    /** How far the spotlight reaches, in blocks. */
    public static final double SPOT_RANGE = 12.0D;
    /** A dim light around the wearer themselves, so they aren't left in the dark while lighting up something further away. */
    public static final int WEARER_LIGHT_LEVEL = 9;

    private static final String TAB = Almura.ID + ":tool";

    private static @Nullable Item item;

    private LightedHelmet() {
    }

    /**
     * Registers the helmet, in the Tools tab as 3.5's.
     */
    public static void registerItems(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        final Item helmet = new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ID))
            .humanoidArmor(ArmorMaterials.IRON, ArmorType.HELMET));
        helper.register(ID, helmet);
        addToTab.accept(TAB, helmet);
        item = helmet;
    }

    /**
     * @return whether the stack (worn on the head) is the lighted helmet
     */
    public static boolean isWearing(final ItemStack head) {
        return item != null && !head.isEmpty() && head.is(item);
    }
}
