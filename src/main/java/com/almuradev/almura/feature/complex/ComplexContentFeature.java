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
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryModifiable;

public final class ComplexContentFeature implements Witness {

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new LightRepairWand());
        event.getRegistry().register(new FarmersAlmanacItem());
    }

    // Note: This section is a complete hack to remove the vanilla recipe for stone brick because it includes 4 variants.  We add mossy and cracked stone brick
    //       so in order for this to work correctly we remove the vanilla recipe that includes all the variants and re-add back just the single recipe.  Re-adding
    //       is required here in order to prevent Advancments from breaking.

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        ResourceLocation StoneBrick = new ResourceLocation("minecraft:stone_brick_stairs");
        IForgeRegistryModifiable modRegistry = (IForgeRegistryModifiable) event.getRegistry();
        modRegistry.remove(StoneBrick);

        GameRegistry.addShapedRecipe(new ResourceLocation("minecraft:stone_brick_stairs"), new ResourceLocation("minecraft:stone_brick_stairs"), new
                        ItemStack(Blocks.STONE_BRICK_STAIRS),
                "AXX",
                "AAX",
                "AAA",
                'A', Blocks.STONEBRICK,
                'X', Blocks.AIR
        );
    }
}
