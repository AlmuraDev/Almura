/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.energy;

/**
 * Used when IndustrialCraft 2 isn't installed: machines run without power.
 */
final class FreeMachineEnergy implements MachineEnergy {

    static final FreeMachineEnergy INSTANCE = new FreeMachineEnergy();

    private FreeMachineEnergy() {
    }

    @Override
    public boolean requiresPower() {
        return false;
    }

    @Override
    public boolean canUse(final double amount) {
        return true;
    }

    @Override
    public boolean use(final double amount) {
        return true;
    }

    @Override
    public double stored() {
        return 0;
    }

    @Override
    public double capacity() {
        return 0;
    }
}
