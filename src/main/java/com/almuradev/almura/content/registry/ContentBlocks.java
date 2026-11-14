/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.CropDef;
import com.almuradev.almura.content.def.ContentDefs.CropStageDef;
import com.almuradev.almura.content.def.ContentDefs.LeafDef;
import com.almuradev.almura.content.def.ContentDefs.SaplingDef;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import com.almuradev.almura.content.def.ContentDefs.Box;
import com.almuradev.almura.content.def.ContentDefs.StateDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Blocks made from content: {@code normal} ones, {@code horizontal} ones (facing the way they were placed from, with their shape turned
 * to match), logs, stairs, slabs and flowers, with 1.12's settings: its materials as the properties they implied, hardness and
 * resistance, light, opacity and shape.
 */
public final class ContentBlocks {

    // 1.12 kept resistance x3 and divided by 5 for explosions; 26.3 takes the explosion resistance
    private static final float RESISTANCE_SCALE = 3f / 5f;

    private ContentBlocks() {
    }

    /**
     * @param stairBase for a stair, the state of the block it is made of
     */
    public static Block create(final BlockDef def, final BlockState stairBase) {
        final BlockBehaviour.Properties properties = properties(def);
        final StateDef state = def.state();
        final VoxelShape shape = shape(state.shape());
        final VoxelShape collision = state.noCollision() ? Shapes.empty() : state.collision() != null ? shape(state.collision()) : shape;
        return switch (def.kind()) {
            case "horizontal" -> new Horizontal(properties, shape, collision);
            case "log" -> new RotatedPillarBlock(properties);
            case "stair" -> new StairBlock(stairBase, properties);
            case "slab" -> new SlabBlock(properties);
            case "flower" -> new Plant(properties.noCollision().pushReaction(PushReaction.POPPED), shape);
            default -> new Normal(properties, shape, collision);
        };
    }

    /**
     * A crop: nothing collides with it, it ticks to grow, it breaks when pushed, and gives the light its stage gives (the mooncheese
     * glows when ripe).
     */
    public static ContentCrop createCrop(final BlockDef def, final CropDef crop) {
        final IntegerProperty age = ContentCrop.ageProperty(crop.maxAge());
        final int[] light = new int[crop.stages().size()];
        boolean glows = false;
        for (final CropStageDef stage : crop.stages()) {
            light[stage.age()] = Math.round(Math.min(1f, Math.max(0f, stage.state().lightEmission())) * 15f);
            glows |= light[stage.age()] > 0;
        }
        final BlockBehaviour.Properties properties = properties(def).noCollision().randomTicks().pushReaction(PushReaction.POPPED);
        if (glows) {
            properties.lightLevel(state -> light[Math.min(state.getValue(age), light.length - 1)]);
        }
        return ContentCrop.create(crop, properties);
    }

    /**
     * Leaves: vanilla's leaves' properties (they tick, don't suffocate, break when pushed), 1.12's leaf hardness of 0.2 unless the content
     * gives one.
     */
    public static ContentLeaves createLeaves(final BlockDef def, final LeafDef leaf) {
        final BlockBehaviour.Properties properties = properties(def).randomTicks().noOcclusion().isSuffocating((state, level, pos) -> false)
            .isRedstoneConductor((state, level, pos) -> false).pushReaction(PushReaction.POPPED);
        if (Float.isNaN(def.state().hardness())) {
            properties.strength(0.2f, Float.isNaN(def.state().resistance()) ? 0.2f : def.state().resistance() * RESISTANCE_SCALE);
        }
        return new ContentLeaves(leaf, properties);
    }

    /**
     * A sapling: nothing collides with it and it ticks to grow.
     */
    public static ContentSapling createSapling(final BlockDef def, final SaplingDef sapling) {
        return new ContentSapling(sapling, properties(def).noCollision().randomTicks().pushReaction(PushReaction.POPPED));
    }

    static BlockBehaviour.Properties properties(final BlockDef def) {
        final StateDef state = def.state();
        final String material = path(def.material());
        final BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, Identifier.parse(def.id())))
            .mapColor(mapColor(def.mapColor(), material))
            .sound(sound(def.sound()));
        // 1.12's defaults: broken at once and no resistance, unless the content says
        final float hardness = Float.isNaN(state.hardness()) ? 0f : state.hardness();
        final float resistance = Float.isNaN(state.resistance()) ? 0f : state.resistance() * RESISTANCE_SCALE;
        properties.strength(hardness, resistance);
        if (state.lightEmission() > 0f) {
            final int light = Math.round(Math.min(1f, state.lightEmission()) * 15f);
            properties.lightLevel(blockState -> light);
        }
        // As on 1.12: a normal or horizontal block hides the faces beside it only when the content says it's opaque (most are OBJ
        // furniture that doesn't fill its space, like chairs); otherwise only when it doesn't say it isn't
        final boolean modelled = def.kind().equals("normal") || def.kind().equals("horizontal");
        final boolean opaque = modelled ? Boolean.TRUE.equals(state.opaque()) : !Boolean.FALSE.equals(state.opaque());
        if (!opaque || material.equals("ag") || def.kind().equals("flower") || def.kind().equals("crop")
            || def.kind().equals("leaf") || def.kind().equals("sapling")) {
            properties.noOcclusion();
        }
        // Rock and metal needed the right tool to drop anything, as on 1.12
        if (material.equals("rock") || material.equals("iron") || material.equals("anvil")) {
            properties.requiresCorrectToolForDrops();
        }
        if (material.equals("wood") || material.equals("cloth") || material.equals("plants") || material.equals("leaves")) {
            properties.ignitedByLava();
        }
        if (material.equals("ag")) {
            // Almura's agriculture material: not solid, broken when pushed
            properties.pushReaction(PushReaction.POPPED);
        }
        return properties;
    }

    private static MapColor mapColor(final @Nullable String color, final String material) {
        final String name = color == null ? material : path(color);
        return switch (name.toLowerCase()) {
            case "dirt", "ground", "ag" -> MapColor.DIRT;
            case "wood" -> MapColor.WOOD;
            case "iron", "metal" -> MapColor.METAL;
            case "sand" -> MapColor.SAND;
            case "grass", "plants", "foliage" -> MapColor.PLANT;
            case "snow" -> MapColor.SNOW;
            case "clay" -> MapColor.CLAY;
            case "water" -> MapColor.WATER;
            case "quartz" -> MapColor.QUARTZ;
            case "gold" -> MapColor.GOLD;
            case "diamond" -> MapColor.DIAMOND;
            case "cloth", "wool" -> MapColor.WOOL;
            default -> MapColor.STONE;
        };
    }

    private static SoundType sound(final @Nullable String sound) {
        return switch (sound == null ? "stone" : path(sound).toLowerCase()) {
            case "wood" -> SoundType.WOOD;
            case "metal" -> SoundType.METAL;
            case "glass" -> SoundType.GLASS;
            case "cloth" -> SoundType.WOOL;
            case "sand" -> SoundType.SAND;
            case "snow" -> SoundType.SNOW;
            case "ground" -> SoundType.GRAVEL;
            case "plant" -> SoundType.GRASS;
            case "ladder" -> SoundType.LADDER;
            case "anvil" -> SoundType.ANVIL;
            case "slime" -> SoundType.SLIME_BLOCK;
            default -> SoundType.STONE;
        };
    }

    static VoxelShape shape(final @Nullable Box box) {
        if (box == null || box.isFullBlock()) {
            return Shapes.block();
        }
        return Block.box(box.minX() * 16, box.minY() * 16, box.minZ() * 16, box.maxX() * 16, box.maxY() * 16, box.maxZ() * 16);
    }

    private static String path(final String id) {
        return id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
    }

    /**
     * A flower: it stands on earth (what vanilla's plants stand on) and drops when that goes, and nothing collides with it, as 1.12's
     * flowers did.
     */
    static final class Plant extends Block {

        private final VoxelShape shape;

        Plant(final BlockBehaviour.Properties properties, final VoxelShape shape) {
            super(properties);
            this.shape = shape;
        }

        @Override
        protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
            return this.shape;
        }

        @Override
        protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
            return level.getBlockState(pos.below()).is(BlockTags.SUPPORTS_VEGETATION);
        }

        @Override
        protected BlockState updateShape(final BlockState state, final LevelReader level, final ScheduledTickAccess ticks, final BlockPos pos,
            final Direction directionToNeighbour, final BlockPos neighbourPos, final BlockState neighbourState, final RandomSource random) {
            return directionToNeighbour == Direction.DOWN && !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
        }
    }

    /**
     * A block that sits as it is.
     */
    static final class Normal extends Block {

        private final VoxelShape shape, collision;

        Normal(final BlockBehaviour.Properties properties, final VoxelShape shape, final VoxelShape collision) {
            super(properties);
            this.shape = shape;
            this.collision = collision;
        }

        @Override
        protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
            return this.shape;
        }

        @Override
        protected VoxelShape getCollisionShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
            return this.collision;
        }
    }

    /**
     * A block that faces toward whoever placed it, as 1.12's horizontal blocks did; its shape, given facing north, turned with it.
     */
    static final class Horizontal extends HorizontalDirectionalBlock {

        private final Map<Direction, VoxelShape> shapes, collisions;

        Horizontal(final BlockBehaviour.Properties properties, final VoxelShape north, final VoxelShape collisionNorth) {
            super(properties);
            this.shapes = Shapes.rotateHorizontal(north);
            this.collisions = Shapes.rotateHorizontal(collisionNorth);
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        }

        @Override
        protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Override
        public BlockState getStateForPlacement(final BlockPlaceContext context) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
            return this.shapes.get(state.getValue(FACING));
        }

        @Override
        protected VoxelShape getCollisionShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
            return this.collisions.get(state.getValue(FACING));
        }
    }
}
