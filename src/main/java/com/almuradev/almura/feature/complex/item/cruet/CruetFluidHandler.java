/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.cruet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

/**
 * Forge fluid handler for a cruet, so tanks, pipes and machines (and {@link net.minecraftforge.fluids.FluidUtil}) can fill and empty
 * it. Like a bucket it is all or nothing: a full {@link Cruets#CAPACITY} goes in or out, swapping the item between the empty and the
 * filled cruet.
 */
public final class CruetFluidHandler implements IFluidHandlerItem, ICapabilityProvider {

    private ItemStack container;

    CruetFluidHandler(final ItemStack container) {
        this.container = container;
    }

    @Nullable
    private FluidStack contents() {
        final Fluid fluid = Cruets.fluidIn(this.container.getItem());
        return fluid == null ? null : new FluidStack(fluid, Cruets.CAPACITY);
    }

    @Override
    public ItemStack getContainer() {
        return this.container;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[] { new FluidTankProperties(this.contents(), Cruets.CAPACITY) };
    }

    @Override
    public int fill(final FluidStack resource, final boolean doFill) {
        if (this.container.getCount() != 1 || resource == null || resource.amount < Cruets.CAPACITY
                || !(this.container.getItem() instanceof GlassCruetItem)) {
            return 0;
        }

        final Item filled = Cruets.filledWith(resource.getFluid());
        if (filled == null) {
            return 0;
        }
        if (doFill) {
            this.container = new ItemStack(filled);
        }
        return Cruets.CAPACITY;
    }

    @Nullable
    @Override
    public FluidStack drain(final FluidStack resource, final boolean doDrain) {
        if (resource == null || !resource.isFluidEqual(this.contents())) {
            return null;
        }
        return this.drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(final int maxDrain, final boolean doDrain) {
        if (this.container.getCount() != 1 || maxDrain < Cruets.CAPACITY) {
            return null;
        }

        final FluidStack contents = this.contents();
        if (contents == null) {
            return null;
        }
        if (doDrain) {
            this.container = Cruets.empty();
        }
        return contents;
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
                ? CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(this) : null;
    }
}
