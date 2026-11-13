/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.Box;
import com.almuradev.almura.content.def.ContentDefs.StateDef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
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
 * Blocks made from content: {@code normal} ones and {@code horizontal} ones (facing the way they were placed from, with their shape turned
 * to match), with 1.12's settings: its materials as the properties they implied, hardness and resistance, light, opacity and shape.
 */
public final class ContentBlocks {

    // 1.12 kept resistance x3 and divided by 5 for explosions; 26.3 takes the explosion resistance
    private static final float RESISTANCE_SCALE = 3f / 5f;

    private ContentBlocks() {
    }

    public static Block create(final BlockDef def) {
        final BlockBehaviour.Properties properties = properties(def);
        final StateDef state = def.state();
        final VoxelShape shape = shape(state.shape());
        final VoxelShape collision = state.collision() != null ? shape(state.collision()) : shape;
        return def.kind().equals("horizontal") ? new Horizontal(properties, shape, collision) : new Normal(properties, shape, collision);
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
        final boolean fullBlock = state.shape() == null || state.shape().isFullBlock();
        if (Boolean.FALSE.equals(state.opaque()) || !fullBlock || material.equals("ag")) {
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
