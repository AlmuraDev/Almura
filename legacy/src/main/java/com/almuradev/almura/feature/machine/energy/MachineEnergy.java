/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Loader;

import java.util.function.Predicate;

/**
 * The power source of a machine. With IndustrialCraft 2 installed this is an EU buffer fed by the IC2 energy net; without it machines run
 * for free. Tile entities forward their lifecycle calls ({@code onLoad}, {@code update}, {@code invalidate}, {@code onChunkUnload}) and NBT
 * to it.
 */
public interface MachineEnergy {

    String IC2_MOD_ID = "ic2";

    /**
     * @param parent the machine's tile entity
     * @param capacity the EU buffer size (ignored without IC2)
     * @param tier the IC2 voltage tier accepted, 1 = LV (32 EU/t) (ignored without IC2)
     * @param acceptsFrom which sides of the machine power can be connected to (ignored without IC2)
     */
    static MachineEnergy create(final TileEntity parent, final double capacity, final int tier, final Predicate<EnumFacing> acceptsFrom) {
        // IC2MachineEnergy (and the IC2 API it uses) is only class loaded when IC2 is installed
        return Loader.isModLoaded(IC2_MOD_ID) ? new IC2MachineEnergy(parent, capacity, tier, acceptsFrom) : FreeMachineEnergy.INSTANCE;
    }

    /**
     * @return false when there is no power system and the machine runs for free
     */
    boolean requiresPower();

    /**
     * @return true when {@code amount} can be used right now
     */
    boolean canUse(double amount);

    /**
     * Uses {@code amount} if available.
     *
     * @return true when it was used
     */
    boolean use(double amount);

    double stored();

    double capacity();

    default void onLoad() {
    }

    default void update() {
    }

    default void invalidate() {
    }

    default void onChunkUnload() {
    }

    default void readFromNBT(final NBTTagCompound compound) {
    }

    default void writeToNBT(final NBTTagCompound compound) {
    }
}
