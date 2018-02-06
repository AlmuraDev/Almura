/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

public enum PageChangeType {
    ADD,
    MODIFY,
    REMOVE;

    public static PageChangeType of(byte type) {
        switch (type) {
            case 0:
                return ADD;
            case 1:
                return MODIFY;
            case 2:
                return REMOVE;
            default:
                return null;
        }
    }
}
