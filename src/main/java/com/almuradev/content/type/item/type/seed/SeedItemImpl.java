/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed;

import com.almuradev.content.component.delegate.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSeeds;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import org.spongepowered.api.block.BlockType;

public final class SeedItemImpl extends ItemSeeds implements SeedItem {

    private final Delegate<BlockType> cropDelegate;
    private final Delegate<BlockType> soilDelegate;

    SeedItemImpl(final SeedItemBuilder builder) {
        // Intentionally passing null here as we'll have it lazy set before using the crop or soil
        super(null, null);
        this.cropDelegate = builder.crop;
        this.soilDelegate = builder.soil;
        builder.fill(this);
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        this.setCatalogsIfNecessary();
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public EnumPlantType getPlantType(final IBlockAccess world, final BlockPos pos) {
        this.setCatalogsIfNecessary();
        return super.getPlantType(world, pos);
    }

    @Override
    public IBlockState getPlant(final IBlockAccess world, final BlockPos pos) {
        this.setCatalogsIfNecessary();
        return super.getPlant(world, pos);
    }

    private void setCatalogsIfNecessary() {
        if (this.crops == null || this.soilBlockID == null) {
            this.crops = (Block) this.cropDelegate.get();
            this.soilBlockID = (Block) this.soilDelegate.get();
        }
    }
}
