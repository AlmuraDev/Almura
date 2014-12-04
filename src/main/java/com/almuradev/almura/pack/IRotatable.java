/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.google.common.collect.Maps;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public interface IRotatable {

    boolean canMirrorRotate(IBlockAccess access, int x, int y, int z, int metadata);

    boolean canRotate(IBlockAccess access, int x, int y, int z, int metadata);

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
        private final int id;
        private static final Map<Integer, Rotation> map = Maps.newHashMap();

        static {
            for (Rotation r : Rotation.values()) {
                map.put(r.getId(), r);
            }
        }

        Rotation(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
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
    }
}
