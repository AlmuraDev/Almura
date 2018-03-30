/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex;

import com.almuradev.almura.feature.complex.item.almanac.item.FarmersAlmanacItem;
import com.almuradev.almura.feature.complex.item.wand.LightRepairWand;
import com.almuradev.core.event.Witness;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryModifiable;

public final class ComplexContentFeature implements Witness {

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new LightRepairWand());
        event.getRegistry().register(new FarmersAlmanacItem());
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        ResourceLocation StoneBrick = new ResourceLocation("minecraft:stone_brick_stairs");
        IForgeRegistryModifiable modRegistry = (IForgeRegistryModifiable) event.getRegistry();
        modRegistry.remove(StoneBrick); // Remove Vanilla Stock Brick recipe because it includes 3 variants of stone brick which is just stupid.
    }
}
