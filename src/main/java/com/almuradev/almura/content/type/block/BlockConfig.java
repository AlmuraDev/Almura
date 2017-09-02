/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block;

public interface BlockConfig {

    String ATTRIBUTES = "attributes";
    String STATES = "states";

    interface Attribute {

        String ITEM_GROUP_DISPLAY = "item-group-display";
    }

    interface State {

        String AABB_KEY = "aabb";
        String ACTION_KEY = "action";
        String GENERIC_KEY = "generic";
        String SOUND_KEY = "sound";

        interface AABB {

            String COLLISION = "collision";
            String WIREFRAME = "wireframe";
            String TYPE = "type";
            String BOX = "box";
        }

        interface Action {

            String BREAK = "break";

            interface Break {

                String WITH = "with";
                String APPLY = "apply";
                String DROPS = "drops";

                interface Drop {

                    String EXPERIENCE = "experience";
                    String ITEM = "item";
                }
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
