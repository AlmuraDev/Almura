/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed;

import com.almuradev.almura.asm.mixin.accessors.item.ItemAccessor;
import com.almuradev.almura.asm.mixin.accessors.item.ItemSeedsAccessor;
import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.item.ItemTooltip;
import com.almuradev.content.type.item.type.seed.processor.grass.Grass;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.block.BlockType;

import java.util.List;

import javax.annotation.Nullable;

import static org.spongepowered.mod.SpongeMod.side;

public final class SeedItemImpl extends ItemSeeds implements SeedItem {
    private final ItemTooltip tooltip = new ItemTooltip.Impl(this);
    private final Delegate<BlockType> cropDelegate;
    private final Delegate<BlockType> soilDelegate;
    @Nullable private final Grass grass;

    SeedItemImpl(final SeedItemBuilder builder) {
        // Intentionally passing null here as we'll have it lazy set before using the crop or soil
        super(null, null);
        this.cropDelegate = builder.crop;
        this.soilDelegate = builder.soil;
        this.grass = builder.grass;
        ((ItemAccessor) (Object) this).accessor$setTabToDisplayOn(null);
        builder.fill(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<String> list, final ITooltipFlag flag) {
        this.tooltip.render(list);
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        this.setCatalogsIfNecessary();
        // This is how we make players use more food/drink...
        player.addExhaustion(0.01F);

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

    @Override
    public Grass getGrass() {
        return this.grass;
    }

    private void setCatalogsIfNecessary() {
        if (((ItemSeedsAccessor) (Object) this).accessor$getCrops() == null || ((ItemSeedsAccessor) (Object) this).accessor$getSoilBlockID() == null) {
            ((ItemSeedsAccessor) (Object) this).accessor$setCrops ((Block) this.cropDelegate.get());
            ((ItemSeedsAccessor) (Object) this).accessor$setSoilBlockID ((Block) this.soilDelegate.get());
        }
    }
}
