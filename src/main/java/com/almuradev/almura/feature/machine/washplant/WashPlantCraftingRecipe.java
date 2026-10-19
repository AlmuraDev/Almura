/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.washplant;

import com.almuradev.almura.feature.machine.MachineBlocks;
import com.almuradev.almura.feature.machine.processing.MachineType;
import ic2.api.item.IC2Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

/**
 * The wash plant's crafting recipe, using IndustrialCraft 2's machine casing and circuit like the old Paydirt one. Only loaded when IC2 is
 * installed, since it uses IC2's API.
 */
public final class WashPlantCraftingRecipe {

    private WashPlantCraftingRecipe() {
    }

    public static void register() {
        @Nullable final ItemStack casing = IC2Items.getItemAPI().getItemStack("resource", "machine");
        @Nullable final ItemStack circuit = IC2Items.getItemAPI().getItemStack("crafting", "circuit");
        final ResourceLocation id = MachineType.WASHPLANT.id();
        GameRegistry.addShapedRecipe(id, id, new ItemStack(MachineBlocks.PROCESSING.get(MachineType.WASHPLANT)),
                " C ",
                " M ",
                " O ",
                'C', circuit == null || circuit.isEmpty() ? Blocks.REDSTONE_BLOCK : circuit,
                'M', casing == null || casing.isEmpty() ? Blocks.IRON_BLOCK : casing,
                'O', Blocks.OBSIDIAN);
    }
}
