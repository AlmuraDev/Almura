/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

public enum GuideOpenType {
    COMMAND,
    PLAYER_INVOKED_KEYBIND,
    PLAYER_LOGGED_IN;

    public static GuideOpenType of(byte type) {
        switch (type) {
            case 0:
                return COMMAND;
            case 1:
                return PLAYER_INVOKED_KEYBIND;
            case 2:
                return PLAYER_LOGGED_IN;
            default:
                return null;
        }
    }
}
