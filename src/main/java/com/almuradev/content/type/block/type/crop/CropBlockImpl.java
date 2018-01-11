/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

import com.almuradev.content.type.block.BlockUpdateFlag;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.Fertilizer;
import com.almuradev.content.type.block.type.crop.processor.growth.Growth;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.kyori.lunar.PrimitiveOptionals;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public final class CropBlockImpl extends BlockCrops implements CropBlock {

    private static final int GROWTH_CHECK_RADIUS = 4;
    private static final Random RANDOM = new Random();
    private final CropBlockStateDefinition[] states;
    private final int maxAge;
    @SuppressWarnings("NullableProblems")
    private PropertyInteger age;

    CropBlockImpl(final CropBlockBuilder builder, final List<CropBlockStateDefinition> states) {
        builder.fill(this);
        this.maxAge = builder.age;
        this.states = new CropBlockStateDefinition[this.maxAge + 1];
        for (final CropBlockStateDefinition state : states) {
            this.states[state.age] = state;
        }
    }

    @Override
    public int getMaxAge() {
        return this.maxAge;
    }

    @Override
    protected PropertyInteger getAgeProperty() {
        return this.age;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected BlockStateContainer createBlockState() {
        if (this.age == null) {
            this.age = CropBlockBuilder.property;
        }
        return new BlockStateContainer(this, this.age);
    }

    private CropBlockStateDefinition state(final IBlockState state) {
        return this.state(state.getValue(this.age));
    }

    public CropBlockStateDefinition state(final int age) {
        return this.states[age];
    }

    @Deprecated
    @Override
    public float getBlockHardness(final IBlockState state, final World world, final BlockPos pos) {
        final CropBlockStateDefinition definition = this.state(state);
        return (float) definition.hardness.orElseGet(() -> super.getBlockHardness(state, world, pos));
    }

    @Deprecated
    @Override
    public int getLightOpacity(final IBlockState state) {
        final CropBlockStateDefinition definition = this.state(state);
        return definition.lightOpacity.orElseGet(() -> super.getLightOpacity(state));
    }

    @Deprecated
    @Override
    public int getLightValue(final IBlockState state) {
        final CropBlockStateDefinition definition = this.state(state);
        return PrimitiveOptionals.mapToInt(definition.lightEmission, value -> (int) (15f * value))
                .orElseGet(() -> super.getLightValue(state));
    }

    @Override
    public int getLightValue(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return this.getLightValue(state);
    }

    @Deprecated
    @Override
    public float getExplosionResistance(final Entity exploder) {
        return (float) this.state(0).resistance.orElse(0);
    }

    @Override
    public float getExplosionResistance(final World world, final BlockPos pos, @Nullable final Entity exploder, final Explosion explosion) {
        final IBlockState state = world.getBlockState(pos);
        final CropBlockStateDefinition definition = this.state(state);
        return definition.resistance.isPresent() ? (float) definition.resistance.getAsDouble() : super.getExplosionResistance(exploder);
    }

    @Deprecated
    @Override
    @SuppressWarnings("ConstantConditions")
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final CropBlockStateDefinition definition = this.state(state);
        return definition.box != null ? definition.box.box() : super.getBoundingBox(state, world, pos);
    }

    @Deprecated
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final CropBlockStateDefinition definition = this.state(state);
        return definition.collisionBox != null ? definition.collisionBox.box() : super.getCollisionBoundingBox(state, world, pos);
    }

    @Deprecated
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public AxisAlignedBB getSelectedBoundingBox(final IBlockState state, final World world, final BlockPos pos) {
        final CropBlockStateDefinition definition = this.state(state);
        return definition.wireFrame != null ? definition.wireFrame.box().offset(pos) : super.getSelectedBoundingBox(state, world, pos);
    }

    @Override
    public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        if (!world.isRemote) {
            // Now you are likely wondering why we don't check max age here. The reason why not is to allow the possibility for fully grown
            // crops to eventually have ways in-which they "rollback" an age (dynamic biome temperatures, ground is no longer fertile, etc)
            this.advanceState(world, pos, state, false);
        }
    }

    @Override
    public boolean canGrow(final World world, final BlockPos pos, final IBlockState state, final boolean client) {
        return !this.isMaxAge(state);
    }

    @Override
    public boolean canUseBonemeal(final World world, final Random random, final BlockPos pos, final IBlockState state) {
        if (!world.isRemote) {
            if (!this.isMaxAge(state)) {
                // TODO Maybe best to move this into an interact listener and make this hook do nothing

                final CropBlockStateDefinition definition = this.state(state);

                if (definition.fertilizer != null) {
                    final Fertilizer fertilizer = definition.fertilizer;

                    // TODO Pre-1.13, this is just a mess...maybe need a Deprecated "LazyItemState" that checks data
                    final DoubleRange chanceRange = fertilizer.getOrLoadChanceRangeForItem(new ItemStack(Items.DYE, 1, 15));

                    if (chanceRange != null) {
                        return RANDOM.nextDouble() <= (chanceRange.random(RANDOM) / 100);
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void grow(final World world, final BlockPos pos, final IBlockState state) {
        if (!world.isRemote) {
            this.advanceState(world, pos, state, true);
        }
    }

    @Override
    public boolean isFertile(final World world, final BlockPos pos) {
        final CropBlockStateDefinition definition = this.state(world.getBlockState(pos.up()));

        final IBlockState soilState = world.getBlockState(pos);

        if (definition.hydration == null) {

            // Vanilla mechanics

            if (soilState.getBlock() == net.minecraft.init.Blocks.FARMLAND)
            {
                return ((Integer)soilState.getValue(BlockFarmland.MOISTURE)) > 0;
            }

            // Not farmland? Seed said our soil is something else so just assume always fertile since their config lacked it
            return true;
        } else {
            final int maxRadius = definition.hydration.getMaxRadius();

            for (final BlockPos.MutableBlockPos inRange : BlockPos.getAllInBoxMutable(
                    pos.add(-maxRadius, 0, -maxRadius),
                    pos.add(maxRadius, 0, maxRadius)
            )) {
                final IBlockState inRangeState;

                // Skip soil state lookup and use it to perform the check. Now you may ask how the soil could be the hydration but our system
                // lets you do stone soil and "hydrated" by stone
                if (inRange.equals(pos)) {
                    inRangeState = soilState;
                } else {
                    inRangeState = world.getBlockState(inRange);
                }

                if (definition.hydration.doesStateMatch(inRangeState)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void advanceState(final World world, final BlockPos pos, final IBlockState state, final boolean fertilizer) {
        final int age = this.getAge(state);
        final CropBlockStateDefinition definition = this.state(age);

        if (fertilizer) {
            world.setBlockState(pos, this.withAge(age + 1), BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
            return;
        }

        final boolean isMaxAge = this.isMaxAge(state);
        final boolean canRollback = definition.canRollback;

        if (isMaxAge && !canRollback) {
            return;
        }

        boolean rollback = false;

        // Crop soil isn't fertile? Don't grow and rollback if applicable
        if (!this.isFertile(world, pos.down())) {
            rollback = true;
        }

        // Check if its time to perform a growth tick
        @Nullable final Growth growth = definition.growth;
        if (!rollback && growth != null) {
            final Biome biome = world.getBiome(pos);

            // Temperature of biome isn't in required range? Don't grow and rollback if applicable
            // TODO Should fertilizer be blocked from advancing a crop if out of temperature? Or should it be allowed
            // TODO and punish the user afterwards (might be more amusing that way)

            final DoubleRange temperatureRequiredRange = growth.getOrLoadTemperatureRequiredRangeForBiome(biome);

            if (temperatureRequiredRange != null) {
                final float biomeTemperature = biome.getTemperature(pos);

                if (!temperatureRequiredRange.contains(biomeTemperature)) {
                    rollback = true;
                }
            }

            // Light of biome isn't in required range? Don't grow and rollback if applicable
            final DoubleRange lightRange = growth.getOrLoadLightRangeForBiome(biome);

            if (lightRange != null) {

                // TODO Split out required light levels based on light source (BLOCK/SKY)?

                final int minLight = (int) lightRange.min();
                final int maxLight = (int) lightRange.max();

                final int areaBlockLight = world.getLightFor(EnumSkyBlock.BLOCK, pos);
                final int worldLight = world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted();

                if ((minLight > areaBlockLight || maxLight < areaBlockLight) && (minLight > worldLight || maxLight < worldLight)) {
                    rollback = true;
                }
            }

            if (!rollback && !this.isMaxAge(state)) {
                final DoubleRange chanceRange = growth.getOrLoadChanceRangeForBiome(biome);

                if (chanceRange == null) {
                    return;
                }

                // Can we grow? Yes? Awesome!
                if (RANDOM.nextDouble() <= (chanceRange.random(RANDOM) / 100) && this.isGrowthEven(world, pos, age)) {
                    // If growth will be even, grow
                    world.setBlockState(pos, this.withAge(age + 1), BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);

                    if (!world.isRemote) {
                        world.playEvent(2005, pos, 0);
                    }

                }
            }
        }

        if (canRollback && rollback) {
            if (age > 0) {
                world.setBlockState(pos, this.withAge(age - 1), BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
            } else {
                // They let the crop continue to roll back? Tough, they just lost it
                // TODO Dockter, do you want to show a generic "dead crop block"
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
            }
        }
    }

    private boolean isGrowthEven(final World world, final BlockPos origin, final int age) {
        for (final BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(
                origin.add(-GROWTH_CHECK_RADIUS, 0, -GROWTH_CHECK_RADIUS),
                origin.add(GROWTH_CHECK_RADIUS, 0, GROWTH_CHECK_RADIUS)
        )) {
            final IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof CropBlockImpl) {
                final CropBlockImpl block = (CropBlockImpl) state.getBlock();
                if (block.getId().equals(this.getId())) {
                    if (block.getAge(state) < age) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
