/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block;

import com.almuradev.almura.content.type.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.type.block.component.aabb.WireFrame;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.ExperienceDrop;
import com.almuradev.almura.content.type.block.component.action.breaks.drop.ItemDrop;
import com.almuradev.almura.content.type.block.component.action.fertilize.FertilizeAction;
import com.almuradev.shared.config.ConfigPath;

public interface BlockConfig {

    String ATTRIBUTES = "attributes";
    String STATES = "states";
    /**
     * @see BlockType
     */
    String TYPE = "type";

    interface Attribute {

        String ITEM_GROUP_DISPLAY = "item-group-display";
    }

    interface State {

        ConfigPath AABB_KEY = new ConfigPath("aabb");
        ConfigPath ACTION_KEY = new ConfigPath("action");
        ConfigPath GENERIC_KEY = new ConfigPath("generic");
        ConfigPath SOUND_KEY = new ConfigPath("sound");
        String PARENT_KEY = "parent";

        interface AABB {

            /**
             * @see CollisionBox
             */
            String COLLISION = "collision";
            /**
             * @see WireFrame
             */
            String WIREFRAME = "wireframe";
            String TYPE = "type";
            String BOX = "box";
        }

        interface Action {

            ConfigPath BREAK = ACTION_KEY.and("break");
            ConfigPath FERTILIZE = ACTION_KEY.and("fertilize");

            interface Break {

                String WITH = "with";
                String APPLY = "apply";
                String DROPS = "drops";

                interface Drop {

                    /**
                     * @see ExperienceDrop
                     */
                    String EXPERIENCE = "experience";
                    /**
                     * @see ItemDrop
                     */
                    String ITEM = "item";
                }
            }

            /**
             * @see FertilizeAction
             */
            interface Fertilize {

                String WITH = "with";
            }
        }

        interface Generic {

            String MAP_COLOR = "map-color";
            String MATERIAL = "material";
            String SLIPPERINESS = "slipperiness";
            String SOUND_GROUP = "sound-group";
            String HARDNESS = "hardness";
            String LIGHT = "light";
            String LIGHT_EMISSION = "emission";
            String LIGHT_OPACITY = "opacity";
            String RESISTANCE = "resistance";
        }

        interface Sound {

            String PARENT = "parent";
            String VOLUME = "volume";
            String PITCH = "pitch";
            String SOUNDS = "sounds";

            interface Sounds {

                String BREAK = "break";
                String STEP = "step";
                String PLACE = "place";
                String HIT = "hit";
                String FALL = "fall";
            }
        }
    }
}
