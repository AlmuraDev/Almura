/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.tileentity;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.common.CommonModule;
import com.almuradev.almura.feature.cache.CacheFeature;
import com.almuradev.almura.shared.capability.ISingleSlotItemHandler;
import com.almuradev.almura.shared.capability.SharedCapabilities;
import com.almuradev.almura.shared.capability.impl.SingleSlotItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public final class SingleSlotTileEntity extends TileEntity {

    private final SingleSlotItemHandler itemHandler = new SingleSlotItemHandler() {
        @Override
        public void onSlotChange() {
            final World world = SingleSlotTileEntity.this.world;
            final BlockPos pos = SingleSlotTileEntity.this.pos;
            final Block blockType = SingleSlotTileEntity.this.blockType;

            if (world != null && !world.isRemote) {
                final IBlockState state = world.getBlockState(pos);
                world.markBlockRangeForRenderUpdate(pos, pos);
                world.notifyBlockUpdate(pos, state, state, 3);
                world.scheduleBlockUpdate(pos, blockType,0,0);
                SingleSlotTileEntity.this.markDirty();
            }
        }
    };

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
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

        if (compound.hasKey("ForgeCaps")) {
            final NBTTagCompound forgeCaps = compound.getCompoundTag("ForgeCaps");

            if (forgeCaps.hasKey(Almura.ID + ":single_slot")) {final NBTTagCompound compoundTag = forgeCaps.getCompoundTag(Almura.ID + ":single_slot");
                SharedCapabilities.SINGLE_SLOT_ITEM_HANDLER_CAPABILITY.readNBT(this.itemHandler, null, compoundTag);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);

        final NBTTagCompound forgeCaps = compound.getCompoundTag("ForgeCaps");
        final Capability<ISingleSlotItemHandler> singleSlotItemHandlerCapability = SharedCapabilities.SINGLE_SLOT_ITEM_HANDLER_CAPABILITY;
        forgeCaps.setTag(Almura.ID + ":single_slot", SharedCapabilities.SINGLE_SLOT_ITEM_HANDLER_CAPABILITY.writeNBT(this.itemHandler, null));

        compound.setTag("ForgeCaps", forgeCaps);
        return compound;
    }
}
