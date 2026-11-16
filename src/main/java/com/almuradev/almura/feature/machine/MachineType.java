/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.recipe.ProcessingRecipes;
import com.almuradev.almura.feature.machine.recipe.SeedProcessorRecipes;
import com.almuradev.almura.feature.machine.recipe.ToolRecipes;
import net.minecraft.resources.Identifier;

/**
 * Almura's processing machines, as 3.5's: they all share the same block, block entity, menu and screen; each only differs in which crafting
 * recipes it runs and whether it waits for Start. 3.5's wash plant ran on IndustrialCraft 2's power, which 4.0 hasn't, so it isn't here.
 */
public enum MachineType {

    /** Crops and ingredients into the seeds a crafting table would give. */
    SEED_PROCESSOR("machine/seed_processor", new SeedProcessorRecipes(), false, false),
    /** Replaces crafting with the knife and the grater. */
    KITCHEN_SLICER("machine/kitchen_slicer", new ToolRecipes("almura:normal/tool/knife", "almura:normal/tool/grater"), true, true),
    /** Replaces crafting with the grinder. */
    KITCHEN_GRINDER("machine/kitchen_grinder", new ToolRecipes("almura:normal/tool/grinder"), true, true);

    private final Identifier id;
    private final ProcessingRecipes recipes;
    private final boolean startOnChoice;
    private final boolean customModel;

    MachineType(final String path, final ProcessingRecipes recipes, final boolean startOnChoice, final boolean customModel) {
        this.id = Identifier.fromNamespaceAndPath(Almura.ID, path);
        this.recipes = recipes;
        this.startOnChoice = startOnChoice;
        this.customModel = customModel;
    }

    /** The block's and item's id. */
    public Identifier id() {
        return this.id;
    }

    /** Server only. */
    public ProcessingRecipes recipes() {
        return this.recipes;
    }

    /**
     * Whether the machine waits for the player to press Start when its input has more than one recipe (so it doesn't begin making the first
     * output before they've picked one). Inputs with a single recipe always start on their own.
     */
    public boolean startOnChoice() {
        return this.startOnChoice;
    }

    /**
     * Whether the block is a shaped (OBJ) model rather than a full cube, so it isn't solid and its outline fits the model.
     */
    public boolean customModel() {
        return this.customModel;
    }

    static MachineType byOrdinal(final int ordinal) {
        final MachineType[] types = values();
        return types[Math.floorMod(ordinal, types.length)];
    }
}
