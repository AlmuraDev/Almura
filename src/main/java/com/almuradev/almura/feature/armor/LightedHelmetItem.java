/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.armor;

import com.almuradev.almura.Almura;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.Sponge;

/**
 * A helmet with a spotlight: it lights up the spot the wearer is looking at, like a flashlight. The light is client side only (see
 * {@code ClientDynamicLightManager}): every client lights up players it can see wearing one, the server's light levels never change.
 *
 * <p>Placeholder stats and look for now: iron helmet protection, durability and texture.</p>
 */
public final class LightedHelmetItem extends ItemArmor {

    public static final ResourceLocation ID = new ResourceLocation(Almura.ID, "armor/lighted_helmet");
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

    LightedHelmetItem() {
        // Render index 2 = iron, matching the placeholder material
        super(ArmorMaterial.IRON, 2, EntityEquipmentSlot.HEAD);
        this.setRegistryName(ID);
        this.setTranslationKey(ID.getPath().replace('/', '.'));
        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":tool").ifPresent(group -> this.setCreativeTab((CreativeTabs) group));
    }

    public static boolean isWearing(final ItemStack head) {
        return !head.isEmpty() && head.getItem() instanceof LightedHelmetItem;
    }
}
