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

    String ITEM_GROUP = "item_group";

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
        /**
         * @see Light
         */
        String LIGHT = "light";
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
