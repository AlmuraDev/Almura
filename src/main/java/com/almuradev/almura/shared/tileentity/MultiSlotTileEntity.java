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
import com.almuradev.content.type.block.type.container.ContainerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
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

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            final BlockPos pos = MultiSlotTileEntity.this.pos;
            final IBlockState state = world.getBlockState(pos);

            String blockName =  state.getBlock().getTranslationKey();
            String stackName = stack.getTranslationKey();

            //System.out.println("New: " + stack.getItem().getRegistryName()); // almura:seed/basil_seed

            //Todo: look at adding the accepted itemStack to the container content config file?

            if (blockName.equalsIgnoreCase("tile.almura.container.agave_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.agave_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.alfalfa_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.alfalfa_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.bambooshoot_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.bambooshoot_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.barley_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.barley_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.basil_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.basil_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.bellpepper_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.bellpepper_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.blackberry_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.blackberry_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.blackroot_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.blackroot_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.blueberry_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.blueberry_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.broccoli_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.broccoli_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.butterbean_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.butterbean_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cabbage_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cabbage_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cantaloupe_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cantaloupe_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cardamon_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cardamon_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.celery_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.celery_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.chilipepper_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.chilipepper_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.chive_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.chive_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cilantro_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cilantro_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cinnamon_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cinnamon_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.clove_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.clove_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cofee_bean_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.coffee_bean_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.concord_grape_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.concord_grape_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.corn_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.corn_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cotton_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cotton_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cranberry_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cranberry_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cucumber_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cucumber_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.cumin_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.cumin_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.daikon_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.daikon_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.dill_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.dill_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.eggplant_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.eggplant_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.fargreen_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.fargreen_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.garlic_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.garlic_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.ginger_root_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.ginger_root_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.honeydew_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.honeydew_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.hop_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.hop_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.laurel_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.laurel_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.leek_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.leek_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.lettuce_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.lettuce_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.lotusroot_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.lotusroot_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.luffa_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.luffa_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.mint_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.mint_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.oat_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.oat_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.olive_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.olive_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.onion_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.onion_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.oregano_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.oregano_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.parsley_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.parsley_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.pea_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.pea_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.peanut_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.peanut_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.peppercorn_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.peppercorn_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.pineapple_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.pineapple_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.popcorn_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.popcorn_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.radish_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.radish_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.raspberry_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.raspberry_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.rice_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.rice_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.rosemary_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.rosemary_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.rye_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.rye_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.sesame_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.sesame_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.sorghum_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.sorghum_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.soybean_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.soybean_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.spinach_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.spinach_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.strawberry_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.strawberry_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.sugarbeet_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.sugarbeet_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.sweetcorn_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.sweetcorn_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.sweetpepper_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.sweetpepper_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.sweetpotato_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.sweetpotato_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.tarragon_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.tarragon_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.thyme_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.thyme_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.tobacco_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.tobacco_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.tomato_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.tomato_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.turnip_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.turnip_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.vanilla_bean_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.vanilla_bean_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (blockName.equalsIgnoreCase("tile.almura.container.white_grape_seed_barrel")) {
                if (stackName.equalsIgnoreCase("item.almura.seed.white_grape_seed")) {
                    return true;
                } else {
                    return false;
                }
            }

            return true;
        }
    };
    public MultiSlotTileEntity() {
        this.itemHandler.setSlotLimit(64);
    }
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

        if (block instanceof ContainerBlock) {
            final int slotCount = ((ContainerBlock) block).getSlotCount();
            itemHandler.resize(slotCount);
        }
    }
}
