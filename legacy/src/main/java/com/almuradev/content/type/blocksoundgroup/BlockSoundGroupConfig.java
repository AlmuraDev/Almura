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
    String PITCH = "pitch";
    String SOUND = "sound";
    String VOLUME = "volume";

    interface Sound {
        String BREAK = "break";
        String FALL = "fall";
        String HIT = "hit";
        String PLACE = "place";
        String STEP = "step";
    }
}
