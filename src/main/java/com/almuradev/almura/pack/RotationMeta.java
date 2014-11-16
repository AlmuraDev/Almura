package com.almuradev.almura.pack;

import net.minecraftforge.common.util.ForgeDirection;

public enum RotationMeta {
    NORTH,
    SOUTH,
    WEST,
    EAST,
    DOWN_NORTH,
    DOWN_SOUTH,
    DOWN_WEST,
    DOWN_EAST,
    UP_NORTH,
    UP_SOUTH,
    UP_WEST,
    UP_EAST;

    public static RotationMeta getRotationMeta(ForgeDirection camera, ForgeDirection player) {
        final int metadata;
        if (camera.getOpposite() == ForgeDirection.DOWN) {
            metadata = 4 + player.getOpposite().ordinal() - 2;
        } else if (camera.getOpposite() == ForgeDirection.UP) {
            metadata = 8 + player.getOpposite().ordinal() - 2;
        } else {
            metadata = camera.getOpposite().ordinal() - 2;
        }

        return getRotationFromMeta(metadata);
    }

    public static RotationMeta getRotationFromMeta(int metadata) {
        for (RotationMeta meta : RotationMeta.values()) {
            if (meta.ordinal() == metadata) {
                return meta;
            }
        }
        return NORTH;
    }
}


