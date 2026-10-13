/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.kitchen.KitchenGrinderTileEntity;
import com.almuradev.almura.feature.machine.kitchen.KitchenSlicerTileEntity;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorRecipes;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorTileEntity;
import com.almuradev.almura.shared.inventory.AlmuraGuiHandler;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Almura's processing machines. They all share the same block, tile entity, container and GUI behaviour ({@link ProcessingBlock},
 * {@link ProcessingTileEntity}, ...); each only differs in which crafting recipes it runs, its name and its GUI id.
 */
public enum MachineType {

    /** Crops and ingredients into the seeds a crafting table would give. */
    SEED_PROCESSOR("machine/seed_processor", AlmuraGuiHandler.SEED_PROCESSOR, SeedProcessorRecipes.INSTANCE, false, false,
            SeedProcessorTileEntity.class, SeedProcessorTileEntity::new),
    /** Replaces crafting with the knife and the grater. */
    KITCHEN_SLICER("machine/kitchen_slicer", AlmuraGuiHandler.KITCHEN_SLICER,
            new ToolRecipes("almura:normal/tool/knife", "almura:normal/tool/grater"), true, true,
            KitchenSlicerTileEntity.class, KitchenSlicerTileEntity::new),
    /** Replaces crafting with the grinder. */
    KITCHEN_GRINDER("machine/kitchen_grinder", AlmuraGuiHandler.KITCHEN_GRINDER, new ToolRecipes("almura:normal/tool/grinder"), true, true,
            KitchenGrinderTileEntity.class, KitchenGrinderTileEntity::new);

    private final ResourceLocation id;
    private final int guiId;
    private final ProcessingRecipes recipes;
    private final boolean startOnChoice;
    private final boolean customModel;
    private final Class<? extends ProcessingTileEntity> tileEntityClass;
    private final Supplier<? extends ProcessingTileEntity> tileEntityFactory;

    MachineType(final String path, final int guiId, final ProcessingRecipes recipes, final boolean startOnChoice, final boolean customModel,
            final Class<? extends ProcessingTileEntity> tileEntityClass, final Supplier<? extends ProcessingTileEntity> tileEntityFactory) {
        this.id = new ResourceLocation(Almura.ID, path);
        this.guiId = guiId;
        this.recipes = recipes;
        this.startOnChoice = startOnChoice;
        this.customModel = customModel;
        this.tileEntityClass = tileEntityClass;
        this.tileEntityFactory = tileEntityFactory;
    }

    /**
     * Whether the machine waits for the player to press Start when its input has more than one recipe (so it doesn't begin making the
     * first output before they've picked one). Inputs with a single recipe always start on their own.
     */
    public boolean startOnChoice() {
        return this.startOnChoice;
    }

    /**
     * Whether the block uses a shaped (OBJ) model rather than a full cube, so it mustn't be treated as a solid cube (that would cut holes in
     * neighbouring blocks) and its hitbox follows the model.
     */
    public boolean customModel() {
        return this.customModel;
    }

    /** The block, item and tile entity registry name. */
    public ResourceLocation id() {
        return this.id;
    }

    public int guiId() {
        return this.guiId;
    }

    public ProcessingRecipes recipes() {
        return this.recipes;
    }

    public Class<? extends ProcessingTileEntity> tileEntityClass() {
        return this.tileEntityClass;
    }

    public ProcessingTileEntity createTileEntity() {
        return this.tileEntityFactory.get();
    }
}
