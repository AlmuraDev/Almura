/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.action.component.drop.Drop;
import com.almuradev.content.type.action.component.drop.ItemDrop;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.BlockUpdateFlag;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.Fertilizer;
import com.almuradev.content.type.block.type.crop.processor.growth.Growth;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.common.SpongeImplHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public final class CropBlockImpl extends BlockCrops implements CropBlock {
    private static final int GROWTH_CHECK_RADIUS = 4;
    private static final Random RANDOM = new Random();
    private static final String HEAT_LAMP_ID = "almura:horizontal/lighting/plant_light";
    @Nullable private static Block heatLamp;
    private static boolean heatLampResolved;
    private final CropBlockStateDefinition[] states;
    private final int maxAge;
    private final Delegate<ItemType> seed;
    // Age a ripe crop is set back to when picked with a harvesting sack, -1 when it can't be
    private final int sackHarvestResetAge;
    @SuppressWarnings("NullableProblems")
    private PropertyInteger age;

    CropBlockImpl(final CropBlockBuilder builder, final List<CropBlockStateDefinition> states) {
        builder.fill(this);
        this.maxAge = builder.age;
        this.seed = builder.seed;
        this.sackHarvestResetAge = builder.sackHarvestResetAge < 0 ? -1 : Math.min(builder.sackHarvestResetAge, Math.max(0, this.maxAge - 1));
        this.setSoundType(SoundType.PLANT);
        this.states = new CropBlockStateDefinition[this.maxAge + 1];
        for (final CropBlockStateDefinition state : states) {
            this.states[state.age] = state;
        }
    }

    @Deprecated
    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public int getMaxAge() {
        return this.maxAge;
    }

    @Override
    public PropertyInteger getAgeProperty() {
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

    @Override
    public CropBlockStateDefinition definition(final IBlockState state) {
        return this.state(state.getValue(this.age));
    }

    public CropBlockStateDefinition state(final int age) {
        return this.states[age];
    }

    /**
     * @return True if this crop is configured for harvesting sacks ({@code sack_harvest}), at any age
     */
    public boolean isSackHarvestable() {
        return this.sackHarvestResetAge >= 0;
    }

    /**
     * @return True if this crop is configured for harvesting sacks ({@code sack_harvest}) and the state is ripe
     */
    public boolean canSackHarvest(final IBlockState state) {
        return this.sackHarvestResetAge >= 0 && state.getBlock() == this && this.isMaxAge(state);
    }

    /**
     * @return The state a ripe crop is set back to after being picked with a harvesting sack
     */
    public IBlockState sackHarvestedState() {
        return this.withAge(Math.max(0, this.sackHarvestResetAge));
    }

    /**
     * @return True if the stack is something this sack harvestable crop can drop (its seed or an item of a stage's destroy drops)
     */
    public boolean isSackHarvestItem(final ItemStack stack) {
        if (this.sackHarvestResetAge < 0 || stack.isEmpty()) {
            return false;
        }

        final ItemType seed = this.seed == null ? null : this.seed.get();
        if (seed != null && stack.getItem() == (Item) seed) {
            return true;
        }

        for (final CropBlockStateDefinition state : this.states) {
            final BlockDestroyAction destroy = state == null || state.destroyAction == null ? null : state.destroyAction.get();
            if (destroy == null) {
                continue;
            }
            for (final BlockDestroyAction.Entry entry : destroy.entries()) {
                for (final Drop drop : entry.drops()) {
                    if (drop instanceof ItemDrop && ((ItemDrop) drop).item().test(stack)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Deprecated
    @Override
    public float getBlockHardness(final IBlockState state, final World world, final BlockPos pos) {
        // Called on every break progress tick, avoid the capturing lambda of orElseGet
        final CropBlockStateDefinition definition = this.definition(state);
        return definition.hardness.isPresent() ? (float) definition.hardness.getAsDouble() : super.getBlockHardness(state, world, pos);
    }

    @Deprecated
    @Override
    public int getLightOpacity(final IBlockState state) {
        // Hot path for the lighting engine (including chunk render threads), avoid allocating
        final CropBlockStateDefinition definition = this.definition(state);
        return definition.lightOpacity.isPresent() ? definition.lightOpacity.getAsInt() : super.getLightOpacity(state);
    }

    @Deprecated
    @Override
    public int getLightValue(final IBlockState state) {
        // Hot path for the lighting engine (including chunk render threads), avoid allocating
        final CropBlockStateDefinition definition = this.definition(state);
        return definition.lightEmission.isPresent() ? (int) (15f * definition.lightEmission.getAsDouble()) : super.getLightValue(state);
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
        final CropBlockStateDefinition definition = this.definition(state);
        return definition.resistance.isPresent() ? (float) definition.resistance.getAsDouble() : super.getExplosionResistance(exploder);
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
                // Return TRUE always when in Dev mode!
                if (SpongeImplHooks.isDeobfuscatedEnvironment()) {
                    return true;
                }
                // TODO Maybe best to move this into an interact listener and make this hook do nothing
                final CropBlockStateDefinition definition = this.definition(state);

                if (definition.fertilizer != null) {
                    final Fertilizer fertilizer = definition.fertilizer;

                    // TODO Pre-1.13, this is just a mess...maybe need a Deprecated "LazyItemState" that checks data
                    final DoubleRange chanceRange = fertilizer.getOrLoadChanceRangeForItem(new ItemStack(Items.DYE, 1, 15));

                    if (chanceRange != null) {
                        return RANDOM.nextDouble() <= (DoubleRanges.random(chanceRange, RANDOM) / 100);
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
        if (!world.isRemote) {
            return this.isFertile(world, pos, this.definition(world.getBlockState(pos.up())));
        }

        return false;
    }

    private boolean isFertile(final World world, final BlockPos pos, final CropBlockStateDefinition definition) {
        final IBlockState soilState = world.getBlockState(pos);

        if (definition.hydration == null) {

            // Vanilla mechanics

            if (soilState.getBlock() == net.minecraft.init.Blocks.FARMLAND) {
                return ((Integer) soilState.getValue(BlockFarmland.MOISTURE)) > 0;
            }

            // Not farmland? Seed said our soil is something else so just assume always fertile since their config lacked it
            return true;
        }

        final int maxRadius = definition.hydration.getMaxRadius();

        for (final BlockPos.MutableBlockPos inRange : BlockPos.getAllInBoxMutable(pos.add(-maxRadius, 0, -maxRadius), pos.add(maxRadius, 0, maxRadius))) {
            // Check the chunk of the position being scanned (not the crop's own) so we never trigger chunk loads at chunk edges
            if (world.isBlockLoaded(inRange)) {
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

    @Nullable
    private static Block heatLamp() {
        // Registry lookup by name is costly and this is called from crop ticks, resolve it once
        if (!heatLampResolved) {
            heatLamp = Block.getBlockFromName(HEAT_LAMP_ID);
            heatLampResolved = true;
        }
        return heatLamp;
    }

    public static boolean hasAdditionalLightSource(World worldIn, BlockPos pos, int type) {
        if (!worldIn.isRemote) {
            // Todo: ability to add more heat/light sources.
            Block block = null;

            if (type == 1) { // Heat Lamp
                block = heatLamp();
            }

            if (block != null) {
                for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-2, 1, -2), pos.add(2, 3, 2))) {
                    // Check the chunk of the position being scanned (not the crop's own) so we never trigger chunk loads at chunk edges
                    if (worldIn.isBlockLoaded(blockpos$mutableblockpos)) {
                        if (worldIn.getBlockState(blockpos$mutableblockpos).getBlock() == block) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Item getSeed() {
        return (Item) this.seed.get();
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player) {
        return new ItemStack(this.getSeed());
    }

    private void advanceState(final World world, final BlockPos pos, final IBlockState state, final boolean fertilizer) {
        final int age = this.getAge(state);
        final CropBlockStateDefinition definition = this.state(age);

        if (fertilizer) {
            world.setBlockState(pos, this.withAge(age + 1), BlockUpdateFlag.UPDATE_CLIENTS);
            return;
        }

        final boolean isMaxAge = this.isMaxAge(state);
        final boolean canRollback = definition.canRollback;

        if (isMaxAge) {  // && !canRollback) { // Remove 3/22/2018 to prevent generated crops from rolling back.
            return;
        }

        boolean rollback = false;

        // Crop soil isn't fertile? Don't grow and rollback if applicable
        if (!this.isFertile(world, pos.down(), definition)) {
            rollback = true;
        }

        // The heat lamp scan reads 75 blocks, so only run it (at most once) when its result actually matters
        @Nullable Boolean hasAdditionalLightSource = null;

        // Check if its time to perform a growth tick
        @Nullable final Growth growth = definition.growth;
        if (!rollback && growth != null) {
            final Biome biome = world.getBiome(pos);
            // Temperature of biome isn't in required range? Don't grow and rollback if applicable
            // TODO Should fertilizer be blocked from advancing a crop if out of temperature? Or should it be allowed
            // TODO and punish the user afterwards (might be more amusing that way)

            final DoubleRange temperatureRequiredRange = growth.getOrLoadTemperatureRequiredRangeForBiome(biome);

            if (temperatureRequiredRange != null) {
                float biomeTemperature = round(biome.getTemperature(pos), 2);

                if (!temperatureRequiredRange.contains(biomeTemperature)) {
                    rollback = true; // Range Check
                }

                if (biomeTemperature < temperatureRequiredRange.min()) {  // Check for additional heat source
                    hasAdditionalLightSource = hasAdditionalLightSource(world, pos, 1);
                    if (hasAdditionalLightSource) {
                        rollback = false;
                    }
                }
            }

            // Light of biome isn't in required range? Don't grow and rollback if applicable
            final DoubleRange light = growth.getOrLoadLightRangeForBiome(biome);
            // Only run light checks if surrounding chunks are loaded else this will trigger chunk loads
            // Skip this section if rollback is true because a Tempoerature fail should never be overridden by light.
            if (!rollback && light != null && world.isAreaLoaded(pos, 1)) {
                final int minLight = (int) light.min();
                final int maxLight = (int) light.max();

                final int lightLevel = world.getLightFromNeighbors(pos);

                if (lightLevel < minLight || lightLevel > maxLight) {
                    if (hasAdditionalLightSource == null) {
                        hasAdditionalLightSource = hasAdditionalLightSource(world, pos, 1);
                    }
                    rollback = !hasAdditionalLightSource;
                }

                if (canRollback && rollback) {
                    if (world.canSeeSky(pos) && !world.isDaytime()) {
                        rollback = false;  // Prevent a crop from rolling back in the middle of the night if it can see sky.
                    }
                }
            }

            if (!rollback && !this.isMaxAge(state)) {
                final DoubleRange biomeGrowthChance = growth.getOrLoadChanceRangeForBiome(biome);

                if (biomeGrowthChance == null || biomeGrowthChance.max() == 0) {
                    return;
                }

                final double randomValue = RANDOM.nextDouble();
                final double growthChance = (DoubleRanges.random(biomeGrowthChance, RANDOM)/ 100);

                boolean canGrow = randomValue <= growthChance;
                // Double the rate of growth if the heat lamp is detected. Only scan for it when the roll falls inside the doubled chance.
                if (!canGrow && randomValue <= growthChance * 2) {
                    if (hasAdditionalLightSource == null) {
                        hasAdditionalLightSource = hasAdditionalLightSource(world, pos, 1);
                    }
                    canGrow = hasAdditionalLightSource;
                }

                // Can we grow? Yes? Awesome!
                if (canGrow && this.isGrowthEven(world, pos, age)) {

                    // Todo: determine why this existed, I don't understand why we would be setting this twice, especially outside of the even check.
                    //world.setBlockState(pos, this.withAge(age + 1), BlockUpdateFlag.UPDATE_CLIENTS);

                    if (ForgeHooks.onCropsGrowPre(world, pos, state, true)) {
                        // If growth will be even, grow
                        world.setBlockState(pos, this.withAge(age + 1), BlockUpdateFlag.UPDATE_CLIENTS);

                        if (!world.isRemote) {
                            world.playEvent(2005, pos, 0);
                        }

                        ForgeHooks.onCropsGrowPost(world, pos, state, null);
                    }
                }
            }
        }

        if (canRollback && rollback) {
            if (!world.isRemote) {
                world.playEvent(2000, pos, 10);  // Smoke
            }
            if (age > 0) {
                world.setBlockState(pos, this.withAge(age - 1), BlockUpdateFlag.UPDATE_CLIENTS);
            } else {
                // They let the crop continue to roll back? Tough, they just lost it
                // TODO Dockter, do you want to show a generic "dead crop block"
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), BlockUpdateFlag.UPDATE_CLIENTS);
            }
        }
    }

    private boolean isGrowthEven(final World world, final BlockPos origin, final int age) {
        for (final BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(
                origin.add(-GROWTH_CHECK_RADIUS, 0, -GROWTH_CHECK_RADIUS),
                origin.add(GROWTH_CHECK_RADIUS, 0, GROWTH_CHECK_RADIUS)
        )) {
            // Never trigger chunk loads at chunk edges, unloaded neighbours can't hold growth back
            if (!world.isBlockLoaded(pos)) {
                continue;
            }
            final IBlockState state = world.getBlockState(pos);
            // Same block instance == same crop id, without building id strings per scanned block
            if (state.getBlock() == this && this.getAge(state) < age) {
                return false;
            }
        }
        return true;
    }

    // Called every crop tick, avoid allocating a BigDecimal just to round
    private static float round(float number, int decimalPlace) {
        final float scale = (float) Math.pow(10, decimalPlace);
        return Math.round(number * scale) / scale;
    }
}
