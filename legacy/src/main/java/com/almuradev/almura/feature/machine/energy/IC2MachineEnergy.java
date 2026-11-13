/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.energy;

import ic2.api.energy.prefab.BasicSink;
import ic2.api.energy.tile.IEnergyEmitter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.function.Predicate;

/**
 * An EU buffer on the IC2 energy net, accepting power only on the machine's power sides. Only class loaded when IC2 is installed.
 */
final class IC2MachineEnergy implements MachineEnergy {

    private final BasicSink sink;

    IC2MachineEnergy(final TileEntity parent, final double capacity, final int tier, final Predicate<EnumFacing> acceptsFrom) {
        this.sink = new BasicSink(parent, capacity, tier) {
            @Override
            public boolean acceptsEnergyFrom(final IEnergyEmitter emitter, final EnumFacing side) {
                // side: the side of the machine the cable or generator is on
                return acceptsFrom.test(side);
            }
        };
    }

    @Override
    public boolean requiresPower() {
        return true;
    }

    @Override
    public boolean canUse(final double amount) {
        return this.sink.canUseEnergy(amount);
    }

    @Override
    public boolean use(final double amount) {
        return this.sink.useEnergy(amount);
    }

    @Override
    public double stored() {
        return this.sink.getEnergyStored();
    }

    @Override
    public double capacity() {
        return this.sink.getCapacity();
    }

    @Override
    public void onLoad() {
        this.sink.onLoad();
    }

    @Override
    public void update() {
        // Joins the energy net on the first tick
        this.sink.update();
    }

    @Override
    public void invalidate() {
        this.sink.invalidate();
    }

    @Override
    public void onChunkUnload() {
        this.sink.onChunkUnload();
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        this.sink.readFromNBT(compound);
    }

    @Override
    public void writeToNBT(final NBTTagCompound compound) {
        this.sink.writeToNBT(compound);
    }
}
