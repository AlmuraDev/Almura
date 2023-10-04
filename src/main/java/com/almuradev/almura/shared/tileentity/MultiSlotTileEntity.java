/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.tileentity;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.storage.block.StorageBlock;
import com.almuradev.almura.shared.capability.IMultiSlotItemHandler;
import com.almuradev.almura.shared.capability.SharedCapabilities;
import com.almuradev.almura.shared.capability.impl.MultiSlotItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public final class MultiSlotTileEntity extends TileEntity {

    private static final String LIMIT_TAG = "Limit";
    private int limit;

    private final MultiSlotItemHandler itemHandler = new MultiSlotItemHandler() {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            final World world = MultiSlotTileEntity.this.world;
            final BlockPos pos = MultiSlotTileEntity.this.pos;
            final Block blockType = MultiSlotTileEntity.this.blockType;


            if (world != null && !world.isRemote) {
                final IBlockState state = world.getBlockState(pos);
                world.markBlockRangeForRenderUpdate(pos, pos);
                world.notifyBlockUpdate(pos, state, state, 3);
                world.scheduleBlockUpdate(pos, blockType, 0, 0);
                MultiSlotTileEntity.this.markDirty();
            }
        }
    };

    public MultiSlotTileEntity(int limit) {
        this.itemHandler.setSlotLimit(limit);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.itemHandler;
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.limit = compound.getInteger(LIMIT_TAG);

        if (compound.hasKey("ForgeCaps")) {
            final NBTTagCompound forgeCaps = compound.getCompoundTag("ForgeCaps");

            if (forgeCaps.hasKey(Almura.ID + ":multi_slot")) {
                final NBTBase tag = forgeCaps.getTag(Almura.ID + ":multi_slot");
                SharedCapabilities.MULTI_SLOT_ITEM_HANDLER_CAPABILITY.readNBT(this.itemHandler, null, tag);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setInteger(LIMIT_TAG, this.limit);

        final NBTTagCompound forgeCaps = compound.getCompoundTag("ForgeCaps");
        forgeCaps.setTag(Almura.ID + ":multi_slot", SharedCapabilities.MULTI_SLOT_ITEM_HANDLER_CAPABILITY.writeNBT(this.itemHandler, null));

        compound.setTag("ForgeCaps", forgeCaps);

        return compound;
    }

    public void setSizeLimit(int limit) {
        this.limit = limit;
    }
    @Override
    public boolean hasFastRenderer() {
        // This should help fast render these since they have no animations.
        return true;
    }

    @Override
    public void onLoad() {
        final IMultiSlotItemHandler itemHandler = (IMultiSlotItemHandler) this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        final IBlockState state = this.world.getBlockState(this.pos);
        final Block block = state.getBlock();
        if (block instanceof StorageBlock) {
            final int slotCount = ((StorageBlock) block).getSlotCount();
            itemHandler.resize(slotCount);
        }
    }
}
