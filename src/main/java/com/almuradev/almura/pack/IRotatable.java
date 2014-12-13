/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.pack.node.RotationNode;
import com.almuradev.almura.pack.node.property.RotationProperty;
import com.google.common.collect.Maps;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public interface IRotatable {

    RotationNode getNode();

    enum Rotation implements IState {
        NORTH(0),
        SOUTH(1),
        WEST(2),
        EAST(3),
        DOWN_NORTH(4),
        DOWN_SOUTH(5),
        DOWN_WEST(6),
        DOWN_EAST(7),
        UP_NORTH(8),
        UP_SOUTH(9),
        UP_WEST(10),
        UP_EAST(11);
        private static final Map<Integer, Rotation> map = Maps.newHashMap();

        static {
            for (Rotation r : Rotation.values()) {
                map.put(r.getId(), r);
            }
        }

        private final int id;

        Rotation(int id) {
            this.id = id;
        }

        public static Rotation getState(ForgeDirection camera, ForgeDirection player) {
            final int metadata;
            if (camera.getOpposite() == ForgeDirection.DOWN) {
                metadata = 4 + player.getOpposite().ordinal() - 2;
            } else if (camera.getOpposite() == ForgeDirection.UP) {
                metadata = 8 + player.getOpposite().ordinal() - 2;
            } else {
                metadata = camera.getOpposite().ordinal() - 2;
            }

            return getState(metadata);
        }

        public static Rotation getState(int id) {
            final Rotation rotation = map.get(id);
            return rotation == null ? Rotation.NORTH : rotation;
        }

        public static Rotation getState(String rawRotation) {
            switch(rawRotation.toUpperCase()) {
                case "NORTH":
                    return NORTH;
                case "SOUTH":
                    return SOUTH;
                case "WEST":
                    return WEST;
                case "EAST":
                    return EAST;
                case "DOWN_NORTH":
                    return DOWN_NORTH;
                case "DOWN_SOUTH":
                    return DOWN_SOUTH;
                case "DOWN_WEST":
                    return DOWN_WEST;
                case "DOWN_EAST":
                    return DOWN_EAST;
                case "UP_NORTH":
                    return UP_NORTH;
                case "UP_SOUTH":
                    return UP_SOUTH;
                case "UP_WEST":
                    return UP_WEST;
                case "UP_EAST":
                    return UP_EAST;
                default:
                    return null;
            }
        }

        @Override
        public int getId() {
            return id;
        }
    }

    enum Direction implements IState {
        BACKWARDS(-1),
        NONE(0),
        FORWARDS(1);

        private static final Map<Integer, Direction> map = Maps.newHashMap();

        static {
            for (Direction r : Direction.values()) {
                map.put(r.getId(), r);
            }
        }

        private final int id;

        Direction(int id) {
            this.id = id;
        }

        public static Direction getState(String rawDirection) {
            switch(rawDirection.toUpperCase()) {
                case "BACKWARDS":
                    return BACKWARDS;
                case "NONE":
                    return NONE;
                case "FORWARDS":
                    return FORWARDS;
                default:
                    return null;
            }
        }

        public static Direction getState(int id) {
            return map.get(id);
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
