/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.energy.MachineEnergy;
import com.almuradev.almura.feature.machine.kitchen.KitchenGrinderTileEntity;
import com.almuradev.almura.feature.machine.kitchen.KitchenSlicerTileEntity;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorRecipes;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorTileEntity;
import com.almuradev.almura.feature.machine.washplant.WashPlantRecipes;
import com.almuradev.almura.feature.machine.washplant.WashPlantTileEntity;
import com.almuradev.almura.shared.inventory.AlmuraGuiHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Loader;

import java.util.function.Supplier;

import javax.annotation.Nullable;

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
            KitchenGrinderTileEntity.class, KitchenGrinderTileEntity::new),
    /** Stone and cobblestone into gravel, or by chance into gold or iron nuggets, using water. Needs IndustrialCraft 2 (it runs on EU). */
    WASHPLANT("machine/washplant", AlmuraGuiHandler.WASHPLANT, WashPlantRecipes.INSTANCE, false, false,
            WashPlantTileEntity.class, WashPlantTileEntity::new, MachineEnergy.IC2_MOD_ID, "machine.washplant", 10_000, 500);

    private final ResourceLocation id;
    private final int guiId;
    private final ProcessingRecipes recipes;
    private final boolean startOnChoice;
    private final boolean customModel;
    private final Class<? extends ProcessingTileEntity> tileEntityClass;
    private final Supplier<? extends ProcessingTileEntity> tileEntityFactory;
    // The mod the machine needs to exist at all, or null
    @Nullable private final String requiredMod;
    @Nullable private final SoundEvent sound;
    // Water tank size and water used per operation, in mB; 0 for machines that don't use water
    private final int waterCapacity;
    private final int waterPerOperation;

    MachineType(final String path, final int guiId, final ProcessingRecipes recipes, final boolean startOnChoice, final boolean customModel,
            final Class<? extends ProcessingTileEntity> tileEntityClass, final Supplier<? extends ProcessingTileEntity> tileEntityFactory) {
        this(path, guiId, recipes, startOnChoice, customModel, tileEntityClass, tileEntityFactory, null, null, 0, 0);
    }

    MachineType(final String path, final int guiId, final ProcessingRecipes recipes, final boolean startOnChoice, final boolean customModel,
            final Class<? extends ProcessingTileEntity> tileEntityClass, final Supplier<? extends ProcessingTileEntity> tileEntityFactory,
            @Nullable final String requiredMod, @Nullable final String sound, final int waterCapacity,
            final int waterPerOperation) {
        this.id = new ResourceLocation(Almura.ID, path);
        this.guiId = guiId;
        this.recipes = recipes;
        this.startOnChoice = startOnChoice;
        this.customModel = customModel;
        this.tileEntityClass = tileEntityClass;
        this.tileEntityFactory = tileEntityFactory;
        this.requiredMod = requiredMod;
        this.sound = sound == null ? null : createSound(sound);
        this.waterCapacity = waterCapacity;
        this.waterPerOperation = waterPerOperation;
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

    /**
     * Whether the machine exists in this game: false when the mod it needs (e.g. IndustrialCraft 2 for the wash plant) isn't installed, in
     * which case its block, item, tile entity, GUI and recipe aren't registered.
     */
    public boolean available() {
        return this.requiredMod == null || Loader.isModLoaded(this.requiredMod);
    }

    /**
     * @return the sound played every second while the machine works, or null
     */
    @Nullable
    public SoundEvent sound() {
        return this.sound;
    }

    /**
     * Whether the machine needs water: it then has a water tank, filled by pipes from above or by water containers in its GUI.
     */
    public boolean usesWater() {
        return this.waterCapacity > 0;
    }

    public int waterCapacity() {
        return this.waterCapacity;
    }

    public int waterPerOperation() {
        return this.waterPerOperation;
    }

    private static SoundEvent createSound(final String path) {
        final ResourceLocation location = new ResourceLocation(Almura.ID, path);
        return new SoundEvent(location).setRegistryName(location);
    }
}
