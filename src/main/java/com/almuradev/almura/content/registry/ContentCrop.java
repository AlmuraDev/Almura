/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.content.def.ContentDefs.CropDef;
import com.almuradev.almura.content.def.ContentDefs.CropStageDef;
import com.almuradev.almura.content.def.ContentDefs.HydrationBlockDef;
import com.almuradev.almura.content.def.ContentDefs.HydrationDef;
import com.almuradev.almura.content.def.ContentDefs.Range;
import com.almuradev.almura.mixin.BiomeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * A crop from the content, growing as 3.5's {@code CropBlockImpl} grew it. On each random tick, until ripe:
 * <ul>
 *     <li>its soil must be watered (its stage's {@code hydration}: a matching block within the radius; without one, farmland that is
 *     wet), its biome warm enough ({@code temperature_required}, or a heat lamp near when it is too cold) and its light in range
 *     ({@code light}, or a heat lamp near);</li>
 *     <li>then it grows a stage at its stage's {@code chance} (percent; twice that under a heat lamp), but only when no crop of its kind
 *     within 4 blocks is behind it, so a field ripens together;</li>
 *     <li>when it can't grow and its stage {@code can_rollback}, it goes back a stage, dying at the first; never at night under the open
 *     sky for want of light.</li>
 * </ul>
 * Bone meal grows it a stage at its stage's bone meal chance. Breaking it drops what its stage lists (the loot table), nothing otherwise.
 */
public final class ContentCrop extends CropBlock {

    private static final int GROWTH_CHECK_RADIUS = 4;
    private static final String HEAT_LAMP_ID = "almura:horizontal/lighting/plant_light";
    // createBlockStateDefinition runs in the super constructor, before this crop's fields are set
    private static final ThreadLocal<IntegerProperty> BUILDING_AGE = new ThreadLocal<>();
    private static @Nullable Block heatLamp;
    private static boolean heatLampResolved;

    private final CropDef def;
    private final IntegerProperty age;
    private final VoxelShape[] shapes;
    private final Identifier seedId, soilId;
    private @Nullable Item seed;

    static ContentCrop create(final CropDef def, final BlockBehaviour.Properties properties) {
        BUILDING_AGE.set(ageProperty(def.maxAge()));
        try {
            return new ContentCrop(def, properties);
        } finally {
            BUILDING_AGE.remove();
        }
    }

    private ContentCrop(final CropDef def, final BlockBehaviour.Properties properties) {
        super(properties);
        this.def = def;
        this.age = BUILDING_AGE.get();
        this.shapes = new VoxelShape[def.stages().size()];
        for (final CropStageDef stage : def.stages()) {
            this.shapes[stage.age()] = ContentBlocks.shape(stage.state().shape());
        }
        this.seedId = Identifier.parse(def.seed().isEmpty() ? "minecraft:air" : def.seed());
        this.soilId = Identifier.parse(def.soil());
    }

    /**
     * 26.3's age property for the number of stages; the content's crops have 4, 5, 6 or 8.
     */
    static IntegerProperty ageProperty(final int maxAge) {
        return switch (maxAge) {
            case 1 -> BlockStateProperties.AGE_1;
            case 2 -> BlockStateProperties.AGE_2;
            case 3 -> BlockStateProperties.AGE_3;
            case 4 -> BlockStateProperties.AGE_4;
            case 5 -> BlockStateProperties.AGE_5;
            case 7 -> BlockStateProperties.AGE_7;
            case 15 -> BlockStateProperties.AGE_15;
            case 25 -> BlockStateProperties.AGE_25;
            default -> IntegerProperty.create("age", 0, maxAge);
        };
    }

    public CropDef def() {
        return this.def;
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return this.age != null ? this.age : BUILDING_AGE.get();
    }

    @Override
    public int getMaxAge() {
        return this.def != null ? this.def.maxAge() : BUILDING_AGE.get().getPossibleValues().getLast();
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BUILDING_AGE.get());
    }

    private CropStageDef stage(final BlockState state) {
        return this.def.stages().get(Math.min(this.getAge(state), this.def.maxAge()));
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return this.shapes[Math.min(this.getAge(state), this.shapes.length - 1)];
    }

    /**
     * Planted on its seed's soil: farmland, or the sand or gravel a few seeds name.
     */
    @Override
    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(this.soilId);
    }

    /**
     * A ripe crop stands on grass too: world generation scatters them in the wild (the content's grass mix). A seed still plants only on
     * its soil.
     */
    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        if (this.isMaxAge(state) && level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
            return true;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        if (this.seed == null) {
            this.seed = BuiltInRegistries.ITEM.getOptional(this.seedId).orElse(Items.AIR);
        }
        return this.seed;
    }

    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        // Ripe crops don't tick (isRandomlyTicking), as 3.5's returned at once
        final int age = this.getAge(state);
        if (age >= this.getMaxAge()) {
            return;
        }
        final CropStageDef stage = this.stage(state);
        boolean rollback = !this.isFertile(level, pos.below(), stage);
        // The heat lamp scan reads 75 blocks: run it at most once, and only when its answer matters
        Boolean heatLampNear = null;
        if (!rollback && (stage.growthChance() != null || stage.light() != null || stage.temperature() != null)) {
            final Range temperature = stage.temperature();
            if (temperature != null) {
                final float biomeTemperature = Math.round(temperature(level, pos) * 100f) / 100f;
                if (!temperature.contains(biomeTemperature)) {
                    rollback = true;
                }
                if (biomeTemperature < temperature.min()) {
                    heatLampNear = heatLampNear(level, pos);
                    if (heatLampNear) {
                        rollback = false;
                    }
                }
            }
            // Only with the neighbours loaded, or the light check loads chunks; a temperature failure is never overridden by light
            final Range light = stage.light();
            if (!rollback && light != null && level.isAreaLoaded(pos, 1)) {
                final int lightLevel = level.getMaxLocalRawBrightness(pos);
                if (lightLevel < (int) light.min() || lightLevel > (int) light.max()) {
                    if (heatLampNear == null) {
                        heatLampNear = heatLampNear(level, pos);
                    }
                    rollback = !heatLampNear;
                }
                // Not in the middle of the night under the open sky
                if (stage.canRollback() && rollback && level.canSeeSky(pos) && !level.isBrightOutside()) {
                    rollback = false;
                }
            }
            if (!rollback) {
                final Range chance = stage.growthChance();
                if (chance == null || chance.max() == 0) {
                    return;
                }
                final double roll = random.nextDouble();
                final double growthChance = chance.random(random.nextDouble()) / 100;
                boolean grows = roll <= growthChance;
                // Twice the chance under a heat lamp; only look for one when the roll falls in the doubled chance
                if (!grows && roll <= growthChance * 2) {
                    if (heatLampNear == null) {
                        heatLampNear = heatLampNear(level, pos);
                    }
                    grows = heatLampNear;
                }
                if (grows && this.isGrowthEven(level, pos, age) && CommonHooks.canCropGrow(level, pos, state, true)) {
                    level.setBlock(pos, state.setValue(this.age, age + 1), Block.UPDATE_CLIENTS);
                    level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
                    CommonHooks.fireCropGrowPost(level, pos, state);
                }
            }
        }
        if (stage.canRollback() && rollback) {
            level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.25, 0.25, 0.25, 0.0);
            if (age > 0) {
                level.setBlock(pos, state.setValue(this.age, age - 1), Block.UPDATE_CLIENTS);
            } else {
                // Let it roll back from its first stage and it's lost
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    /**
     * Whether the soil is watered for this stage: a {@code hydration} block within its radius of the soil, on its level (the soil itself
     * at radius 0); without one, vanilla's wet farmland, or any other soil.
     */
    private boolean isFertile(final Level level, final BlockPos soil, final CropStageDef stage) {
        final HydrationDef hydration = stage.hydration();
        if (hydration == null) {
            final BlockState soilState = level.getBlockState(soil);
            return !(soilState.getBlock() instanceof FarmlandBlock) || soilState.getValue(FarmlandBlock.MOISTURE) > 0;
        }
        final int radius = hydration.radius();
        for (final BlockPos at : BlockPos.betweenClosed(soil.offset(-radius, 0, -radius), soil.offset(radius, 0, radius))) {
            // Never load a chunk at the edge of the loaded ones
            if (level.isLoaded(at) && matches(hydration, level.getBlockState(at))) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(final HydrationDef hydration, final BlockState state) {
        final Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        for (final HydrationBlockDef block : hydration.blocks()) {
            if (!id.toString().equals(block.block())) {
                continue;
            }
            boolean all = true;
            for (final Map.Entry<String, Range> property : block.properties().entrySet()) {
                final Property<?> found = state.getBlock().getStateDefinition().getProperty(property.getKey());
                // A property the block hasn't is ignored, as 3.5 ignored it (the content asks farmland for "level")
                if (found != null && !(state.getValue(found) instanceof final Integer value && property.getValue().contains(value))) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return true;
            }
        }
        return false;
    }

    /**
     * The biome's temperature here, adjusted for height as 1.12's {@code Biome.getTemperature(pos)} was.
     */
    private static float temperature(final ServerLevel level, final BlockPos pos) {
        final Biome biome = level.getBiome(pos).value();
        return ((BiomeAccessor) (Object) biome).almura$getTemperature(pos, level.getSeaLevel());
    }

    /**
     * Whether a heat lamp ({@code plant_light}) hangs over the crop: 2 around, 1 to 3 above.
     */
    private static boolean heatLampNear(final Level level, final BlockPos pos) {
        if (!heatLampResolved) {
            heatLamp = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(HEAT_LAMP_ID)).orElse(null);
            heatLampResolved = true;
        }
        if (heatLamp == null) {
            return false;
        }
        for (final BlockPos at : BlockPos.betweenClosed(pos.offset(-2, 1, -2), pos.offset(2, 3, 2))) {
            if (level.isLoaded(at) && level.getBlockState(at).is(heatLamp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether no crop of this kind within 4 blocks (on its level, loaded) is a stage behind, so a field ripens together.
     */
    private boolean isGrowthEven(final Level level, final BlockPos origin, final int age) {
        for (final BlockPos at : BlockPos.betweenClosed(origin.offset(-GROWTH_CHECK_RADIUS, 0, -GROWTH_CHECK_RADIUS),
            origin.offset(GROWTH_CHECK_RADIUS, 0, GROWTH_CHECK_RADIUS))) {
            if (!level.isLoaded(at)) {
                continue;
            }
            final BlockState state = level.getBlockState(at);
            if (state.is(this) && this.getAge(state) < age) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValidBonemealTarget(final LevelReader level, final BlockPos pos, final BlockState state, final BonemealSource source) {
        return !this.isMaxAge(state);
    }

    /**
     * At the stage's bone meal chance; never for a stage that takes none.
     */
    @Override
    public boolean isBonemealSuccess(final Level level, final RandomSource random, final BlockPos pos, final BlockState state,
        final BonemealSource source) {
        final Range chance = this.stage(state).bonemealChance();
        return chance != null && random.nextDouble() <= chance.random(random.nextDouble()) / 100;
    }

    /**
     * One stage, as 3.5's fertilizer grew it (vanilla's bone meal grows two to five).
     */
    @Override
    public void performBonemeal(final ServerLevel level, final RandomSource random, final BlockPos pos, final BlockState state,
        final BonemealSource source) {
        level.setBlock(pos, state.setValue(this.age, Math.min(this.getMaxAge(), this.getAge(state) + 1)), Block.UPDATE_CLIENTS);
    }
}
