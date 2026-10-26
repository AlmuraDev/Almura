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
import com.almuradev.content.type.block.ModelHiddenPropertiesBlock;
import com.almuradev.content.type.block.type.crop.processor.fertilizer.Fertilizer;
import com.almuradev.content.type.block.type.crop.processor.growth.Growth;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public final class CropBlockImpl extends BlockCrops implements CropBlock, ModelHiddenPropertiesBlock {
    private static final int GROWTH_CHECK_RADIUS = 4;
    /*
     * Aquatic crops in water are water material blocks (so water treats them as water: it doesn't flow into them, doesn't draw faces against
     * them, and entities in them swim), and vanilla reads BlockLiquid.LEVEL from any water material block (flow, buckets, boats...), so they
     * carry that property too, fixed at 0 like a still source: any other level and the water around them slopes and flows towards them.
     * (Buckets would take a level 0 block: AquaticCropBuckets stops them.)
     */
    private static final int AQUATIC_LEVEL = 0;
    // Water's light opacity: crops in water filter light like it, so grass can't spread onto the dirt under them (it needs 2 or less above)
    private static final int WATER_LIGHT_OPACITY = 3;
    /**
     * A surface aquatic crop's wet flag: true when it was planted in water (and is a water block), false for one planted on farmland before
     * the crop was aquatic (which keeps growing there as a land crop). Kept in the meta's top bit, so blocks saved before read as dry.
     */
    public static final PropertyBool WET = PropertyBool.create("wet");
    private static final int WET_META_BIT = 8;
    private static final Random RANDOM = new Random();
    private static final String HEAT_LAMP_ID = "almura:horizontal/lighting/plant_light";
    @Nullable private static Block heatLamp;
    private static boolean heatLampResolved;
    private final CropBlockStateDefinition[] states;
    private final int maxAge;
    private final Delegate<ItemType> seed;
    // Age a ripe crop is set back to when picked with a harvesting sack, -1 when it can't be
    private final int sackHarvestResetAge;
    private final AquaticMode aquatic;
    @SuppressWarnings("NullableProblems")
    private PropertyInteger age;

    CropBlockImpl(final CropBlockBuilder builder, final List<CropBlockStateDefinition> states) {
        builder.fill(this);
        this.aquatic = builder.aquatic;
        if (this.aquatic != AquaticMode.NONE) {
            this.setDefaultState(this.getDefaultState().withProperty(BlockLiquid.LEVEL, AQUATIC_LEVEL));
        }
        if (this.aquatic == AquaticMode.SURFACE) {
            this.setDefaultState(this.getDefaultState().withProperty(WET, false));
        }
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
    public boolean canRenderInLayer(final IBlockState state, final BlockRenderLayer layer) {
        // A wet surface crop draws its own water surface, which (like water) needs the translucent pass; the plant draws there with it
        if (this.aquatic == AquaticMode.SURFACE && state.getValue(WET)) {
            return layer == BlockRenderLayer.TRANSLUCENT;
        }
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        if (this.aquatic == AquaticMode.SURFACE) {
            return this.getAge(state) | (state.getValue(WET) ? WET_META_BIT : 0);
        }
        return super.getMetaFromState(state);
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        if (this.aquatic == AquaticMode.SURFACE) {
            return this.withAge(Math.min(meta & (WET_META_BIT - 1), this.maxAge)).withProperty(WET, (meta & WET_META_BIT) != 0);
        }
        return super.getStateFromMeta(meta);
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
        // Called from the super constructor, before this crop's fields are set: the builder says how it grows in water
        switch (CropBlockBuilder.buildingAquatic) {
            case SUBMERGED:
                return new BlockStateContainer(this, this.age, BlockLiquid.LEVEL);
            case SURFACE:
                return new BlockStateContainer(this, this.age, BlockLiquid.LEVEL, WET);
            default:
                return new BlockStateContainer(this, this.age);
        }
    }

    @Override
    public Collection<IProperty<?>> modelHiddenProperties() {
        // The water level of an aquatic crop never changes its look, so its blockstate file doesn't list it (a surface crop's wet flag does
        // change its look: wet, it draws a water surface)
        return this.aquatic != AquaticMode.NONE ? Collections.singletonList(BlockLiquid.LEVEL) : Collections.emptyList();
    }

    /**
     * @return How this crop grows in water ({@code aquatic})
     */
    public AquaticMode aquaticMode() {
        return this.aquatic;
    }

    /**
     * @return True if this crop is planted in water ({@code aquatic})
     */
    public boolean isAquatic() {
        return this.aquatic != AquaticMode.NONE;
    }

    /**
     * @return True if the state is in water (a water material block): every state of a submerged crop, the wet states of a surface one
     */
    public boolean isWaterState(final IBlockState state) {
        return this.aquatic == AquaticMode.SUBMERGED || (this.aquatic == AquaticMode.SURFACE && state.getBlock() == this && state.getValue(WET));
    }

    /**
     * The state to plant an aquatic crop as at the position, or null if it can't be planted there. Both kinds go into still water (a source) on
     * the bed: a submerged crop needs water above it too (so it's always under the surface, where a water material block would leave a gap in
     * the water's top face), a surface crop needs air above it (it sticks out, and draws the surface itself).
     */
    @Nullable
    public IBlockState aquaticPlantingState(final World world, final BlockPos pos) {
        final IBlockState at = world.getBlockState(pos);
        if (!(at.getBlock() instanceof BlockLiquid) || at.getMaterial() != Material.WATER || at.getValue(BlockLiquid.LEVEL) != 0
                || !isBed(world.getBlockState(pos.down()))) {
            return null;
        }
        switch (this.aquatic) {
            case SUBMERGED:
                return world.getBlockState(pos.up()).getMaterial() == Material.WATER ? this.getDefaultState() : null;
            case SURFACE:
                return world.isAirBlock(pos.up()) ? this.getDefaultState().withProperty(WET, true) : null;
            default:
                return null;
        }
    }

    private static boolean isBed(final IBlockState state) {
        final Material material = state.getMaterial();
        // Sand and gravel (sand), dirt and farmland (ground), clay
        return material == Material.SAND || material == Material.GROUND || material == Material.CLAY;
    }

    @Deprecated
    @Override
    public Material getMaterial(final IBlockState state) {
        return this.isWaterState(state) ? Material.WATER : super.getMaterial(state);
    }

    @Override
    public boolean isReplaceable(final IBlockAccess world, final BlockPos pos) {
        // Water material is replaceable, which would let any block be placed straight over a crop in water
        return !this.isWaterState(world.getBlockState(pos)) && super.isReplaceable(world, pos);
    }

    @Override
    protected boolean canSustainBush(final IBlockState state) {
        return this.aquatic != AquaticMode.NONE ? isBed(state) : super.canSustainBush(state);
    }

    @Override
    public boolean canBlockStay(final World world, final BlockPos pos, final IBlockState state) {
        if (this.isWaterState(state)) {
            final Material above = world.getBlockState(pos.up()).getMaterial();
            // Submerged: water above it. Surface: not under water (it sticks out into the air)
            return isBed(world.getBlockState(pos.down())) && (this.aquatic == AquaticMode.SUBMERGED) == (above == Material.WATER);
        }
        return super.canBlockStay(world, pos, state);
    }

    @Override
    protected void checkAndDropBlock(final World world, final BlockPos pos, final IBlockState state) {
        if (this.isWaterState(state) && !this.canBlockStay(world, pos, state)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockState(pos, Blocks.WATER.getDefaultState(), BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
            return;
        }
        super.checkAndDropBlock(world, pos, state);
    }

    @Override
    public boolean removedByPlayer(final IBlockState state, final World world, final BlockPos pos, final EntityPlayer player, final boolean willHarvest) {
        if (this.isWaterState(state)) {
            // Back to the water it grew in, not an air pocket
            return world.setBlockState(pos, Blocks.WATER.getDefaultState(), world.isRemote ? 11 : 3);
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    /**
     * @return What's left when this crop is gone: water for one in water, air otherwise
     */
    private IBlockState emptyState(final IBlockState state) {
        return this.isWaterState(state) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
    }

    /**
     * @return The state at a different age, keeping everything else (a surface crop's wet flag)
     */
    private IBlockState aged(final IBlockState state, final int age) {
        return state.withProperty(this.age, age);
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
     * @return The state a ripe crop is set back to after being picked with a harvesting sack (keeping a surface crop's wet flag)
     */
    public IBlockState sackHarvestedState(final IBlockState ripe) {
        return this.aged(ripe.getBlock() == this ? ripe : this.getDefaultState(), Math.max(0, this.sackHarvestResetAge));
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
        if (definition.lightOpacity.isPresent()) {
            return definition.lightOpacity.getAsInt();
        }
        return this.isWaterState(state) ? WATER_LIGHT_OPACITY : super.getLightOpacity(state);
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
            world.setBlockState(pos, this.aged(state, age + 1), BlockUpdateFlag.UPDATE_CLIENTS);
            return;
        }

        final boolean isMaxAge = this.isMaxAge(state);
        final boolean canRollback = definition.canRollback;

        if (isMaxAge) {  // && !canRollback) { // Remove 3/22/2018 to prevent generated crops from rolling back.
            return;
        }

        boolean rollback = false;

        // Crop soil isn't fertile? Don't grow and rollback if applicable
        // (A crop in water is always watered: the hydration check is for land crops)
        if (!this.isWaterState(state) && !this.isFertile(world, pos.down(), definition)) {
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
                        world.setBlockState(pos, this.aged(state, age + 1), BlockUpdateFlag.UPDATE_CLIENTS);

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
                world.setBlockState(pos, this.aged(state, age - 1), BlockUpdateFlag.UPDATE_CLIENTS);
            } else {
                // They let the crop continue to roll back? Tough, they just lost it
                // TODO Dockter, do you want to show a generic "dead crop block"
                world.setBlockState(pos, this.emptyState(state), BlockUpdateFlag.UPDATE_CLIENTS);
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
