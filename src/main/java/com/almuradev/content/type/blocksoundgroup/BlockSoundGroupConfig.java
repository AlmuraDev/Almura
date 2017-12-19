/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup;

public interface BlockSoundGroupConfig {

    String PARENT = "parent";
    String VOLUME = "volume";
    String PITCH = "pitch";
    String SOUND = "sound";

    interface Sound {

        String BREAK = "break";
        String STEP = "step";
        String PLACE = "place";
        String HIT = "hit";
        String FALL = "fall";
    }
}
