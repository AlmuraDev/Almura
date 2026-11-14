/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.def.ContentDefs.BiomeRange;
import com.almuradev.almura.content.def.ContentDefs.GrassDef;
import com.almuradev.almura.content.def.ContentDefs.GrassEntryDef;
import com.almuradev.almura.content.def.ContentDefs.GrassGenDef;
import com.almuradev.almura.content.def.ContentDefs.Range;
import com.almuradev.almura.content.def.ContentDefs.StateRef;
import com.almuradev.almura.content.def.ContentDefs.TreeDef;
import com.almuradev.almura.content.def.ContentDefs.TreeGenDef;
import com.almuradev.almura.mixin.BiomeAccessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * The content's world generation, as 3.5's {@code TreeGeneratorImpl} and {@code GrassGeneratorImpl} did it on 1.12's decoration events:
 * <ul>
 *     <li>{@code almura:content_trees}: once a chunk, each tree generation in turn rolls its chance for the biome of a random surface spot;
 *     the first to pass grows its tree there, and vanilla grows no trees in that chunk;</li>
 *     <li>{@code almura:content_grass}: twice a chunk (1.12 asked once for flowers and once for grass), each grass generation rolls its
 *     chance; when it passes its mix is scattered around a random surface spot, and vanilla places no flowers (or no grass) in that chunk.</li>
 * </ul>
 * Both run in the step before vanilla's vegetation ({@code fluid_springs}) so they go first; vanilla's trees, grass and flowers ask
 * {@link #taken} (see {@code TreeFeatureMixin} and {@code SimpleBlockFeatureMixin}).
 */
public final class ContentGeneration {

    public enum Kind { TREES, GRASS, FLOWERS }

    private static final List<TreeGenDef> TREES = new ArrayList<>();
    private static final List<GrassGenDef> GRASSES = new ArrayList<>();
    private static final Map<String, GrassDef> GRASS_MIXES = new java.util.HashMap<>();
    // What Almura took in each chunk being decorated (a WorldGenRegion is one chunk's decoration)
    private static final Map<WorldGenRegion, java.util.EnumSet<Kind>> TAKEN = Collections.synchronizedMap(new WeakHashMap<>());
    // Where a tree may grow unless its config says otherwise: anywhere it doesn't snow (vanilla snows below 0.15)
    private static final double MIN_TEMPERATURE = 0.15;

    private ContentGeneration() {
    }

    public static void register(final IEventBus modBus, final List<TreeGenDef> trees, final List<GrassGenDef> grasses, final List<GrassDef> mixes) {
        TREES.addAll(trees);
        GRASSES.addAll(grasses);
        mixes.forEach(mix -> GRASS_MIXES.put(mix.id(), mix));
        final DeferredRegister<MapCodec<? extends Feature>> types = DeferredRegister.create(Registries.FEATURE_TYPE, Almura.ID);
        types.register("content_trees", () -> Trees.CODEC);
        types.register("content_grass", () -> Grass.CODEC);
        types.register(modBus);
    }

    /**
     * Whether Almura already placed its trees, grass or flowers in the chunk this world generation is decorating.
     */
    public static boolean taken(final LevelAccessor level, final Kind kind) {
        if (!(level instanceof final WorldGenRegion region)) {
            return false;
        }
        final java.util.EnumSet<Kind> taken = TAKEN.get(region);
        return taken != null && taken.contains(kind);
    }

    private static void take(final WorldGenLevel level, final Kind kind) {
        if (level instanceof final WorldGenRegion region) {
            TAKEN.computeIfAbsent(region, key -> java.util.EnumSet.noneOf(Kind.class)).add(kind);
        }
    }

    private static String biome(final WorldGenLevel level, final BlockPos pos) {
        return ContentTrees.biome(level, pos);
    }

    private static boolean roll(final List<BiomeRange> chances, final String biome, final RandomSource random) {
        final Range chance = BiomeRange.pick(chances, biome);
        return chance != null && chance.max() > 0 && random.nextDouble() <= chance.random(random.nextDouble()) / 100;
    }

    /**
     * A random spot in the chunk, on its surface (the top block that stops movement or holds water, as 1.12's height did).
     */
    private static BlockPos surface(final WorldGenLevel level, final BlockPos chunkOrigin, final RandomSource random) {
        final int x = chunkOrigin.getX() + random.nextInt(16);
        final int z = chunkOrigin.getZ() + random.nextInt(16);
        return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
    }

    /**
     * The content's trees: the first whose chance passes grows, and vanilla's then grow none in the chunk.
     */
    public record Trees() implements Feature {

        public static final MapCodec<Trees> CODEC = MapCodec.unit(Trees::new);

        @Override
        public MapCodec<? extends Feature> codec() {
            return CODEC;
        }

        @Override
        public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
            for (final TreeGenDef generation : TREES) {
                if (taken(level, Kind.TREES)) {
                    return true;
                }
                final BlockPos pos = surface(level, origin, random);
                final String biome = biome(level, pos);
                if (!roll(generation.chance(), biome, random) || !climateAllows(level, pos)) {
                    continue;
                }
                final TreeDef tree = ContentTrees.tree(generation.tree());
                if (tree != null) {
                    ContentTrees.grow(level, pos, tree, random, generation.requires(), Block.UPDATE_CLIENTS);
                    // 1.12 denied vanilla's trees whether or not this one found room
                    take(level, Kind.TREES);
                }
            }
            return true;
        }

        /**
         * Warm enough not to snow, and never on snow or ice.
         */
        private static boolean climateAllows(final WorldGenLevel level, final BlockPos pos) {
            final float temperature = ((BiomeAccessor) (Object) level.getBiome(pos).value()).almura$getTemperature(pos, level.getSeaLevel());
            if (temperature < MIN_TEMPERATURE) {
                return false;
            }
            final BlockState ground = level.getBlockState(pos.below());
            return !ground.is(BlockTags.ICE) && !ground.is(Blocks.SNOW_BLOCK) && !ground.is(Blocks.POWDER_SNOW) && !ground.is(Blocks.SNOW)
                && !level.getBlockState(pos).is(Blocks.SNOW);
        }
    }

    /**
     * The content's grass mixes: rolled once for vanilla's flowers and once for its grass, each pass taking that part of the chunk.
     */
    public record Grass() implements Feature {

        public static final MapCodec<Grass> CODEC = MapCodec.unit(Grass::new);

        @Override
        public MapCodec<? extends Feature> codec() {
            return CODEC;
        }

        @Override
        public boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin) {
            for (final Kind kind : new Kind[] {Kind.FLOWERS, Kind.GRASS}) {
                for (final GrassGenDef generation : GRASSES) {
                    if (taken(level, kind)) {
                        break;
                    }
                    final GrassDef mix = GRASS_MIXES.get(generation.grass());
                    if (mix == null || mix.entries().isEmpty() || !roll(generation.chance(), biome(level, origin.offset(8, 0, 8)), random)) {
                        continue;
                    }
                    scatter(level, surface(level, origin, random), mix, generation.requires(), random);
                    take(level, kind);
                }
            }
            return true;
        }

        /**
         * 3.5's {@code GrassFeature}: down through air and leaves to the ground, then 128 tries around it (8 out, 4 up and down), each
         * into air, of an entry picked by weight, standing on a required block and where it can stand.
         */
        private static void scatter(final WorldGenLevel level, final BlockPos start, final GrassDef mix, final List<String> requires,
            final RandomSource random) {
            BlockPos origin = start;
            while (origin.getY() > level.getMinY() && (level.getBlockState(origin).isAir() || level.getBlockState(origin).is(BlockTags.LEAVES))) {
                origin = origin.below();
            }
            final int totalWeight = mix.entries().stream().mapToInt(GrassEntryDef::weight).sum();
            for (int i = 0; i < 128; i++) {
                final BlockPos target = origin.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4),
                    random.nextInt(8) - random.nextInt(8));
                if (!level.getBlockState(target).isAir()) {
                    continue;
                }
                final BlockState state = pick(mix, totalWeight, random);
                if (state == null) {
                    continue;
                }
                if (!requires.isEmpty() && !requires.contains(BuiltInRegistries.BLOCK.getKey(level.getBlockState(target.below()).getBlock()).toString())) {
                    continue;
                }
                if (!canStand(level, target, state)) {
                    continue;
                }
                if (state.getBlock() instanceof DoublePlantBlock) {
                    if (level.getBlockState(target.above()).isAir()) {
                        DoublePlantBlock.placeAt(level, state, target, Block.UPDATE_CLIENTS);
                    }
                } else {
                    level.setBlock(target, state, Block.UPDATE_CLIENTS);
                }
            }
        }

        /**
         * Whether the plant can stand there; a ripe crop of the content's stands on grass in the wild (see {@link ContentCrop#canSurvive}).
         */
        private static boolean canStand(final WorldGenLevel level, final BlockPos pos, final BlockState state) {
            return state.canSurvive(level, pos);
        }

        private static @Nullable BlockState pick(final GrassDef mix, final int totalWeight, final RandomSource random) {
            if (totalWeight <= 0) {
                return null;
            }
            int roll = random.nextInt(totalWeight);
            for (final GrassEntryDef entry : mix.entries()) {
                roll -= entry.weight();
                if (roll < 0) {
                    return state(entry.blocks().get(random.nextInt(entry.blocks().size())));
                }
            }
            return null;
        }
    }

    /**
     * A block state the content names, or null when its block doesn't exist; properties it hasn't, or values it can't take, are left as
     * they are.
     */
    static @Nullable BlockState state(final StateRef ref) {
        final Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(ref.block()));
        if (block.isEmpty() || block.get() == Blocks.AIR) {
            return null;
        }
        BlockState state = block.get().defaultBlockState();
        if (block.get() instanceof final ContentCrop crop) {
            // A wild crop is a ripe one: the content names an age for each, nine of them a stage short of ripe (or past it)
            return crop.getStateForAge(crop.getMaxAge());
        }
        for (final Map.Entry<String, String> property : ref.properties().entrySet()) {
            state = withValue(state, block.get().getStateDefinition().getProperty(property.getKey()), property.getValue());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState withValue(final BlockState state, final @Nullable Property<T> property, final String value) {
        if (property == null) {
            return state;
        }
        return property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElse(state);
    }
}
