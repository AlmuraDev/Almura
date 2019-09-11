/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.content.type.action.ActionConfig;
import com.almuradev.content.type.block.component.aabb.AABBConfig;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroupConfig;

public interface BlockConfig {
    String STATE = "state";
    String DEFAULT_STATE_NAME = "normal";

    String MAP_COLOR = "map_color";
    String MATERIAL = "material";

    String EFFECTIVE_TOOLS = "effective_tools";

    String ITEM_GROUP = "item_group";
    String RENDER_LAYER = "render_layer";

    String IS_FULL_BLOCK = "is_full_block";
    String IS_NORMAL_CUBE = "is_normal_cube";
    String IS_TOP_SOLID = "is_top_solid";

    interface State {
        String PARENT = "parent";

        /**
         * @see AABBConfig
         */
        String AABB = "aabb";
        /**
         * @see ActionConfig
         */
        String ACTION = "action";
        String BLOCK_FACE_SHAPE = "block_face_shape";
        String HARDNESS = "hardness";
        String FLAMMABILITY = "flammability";
        String FIRE_SPREAD_SPEED = "fire_spread_speed";
        /**
         * @see Light
         */
        String LIGHT = "light";
        String OPAQUE = "opaque";
        String RESISTANCE = "resistance";
        /**
         * @see BlockSoundGroupConfig
         */
        String SOUND = "sound";

        interface Light {
            String EMISSION = "emission";
            String OPACITY = "opacity";
        }
    }
}
