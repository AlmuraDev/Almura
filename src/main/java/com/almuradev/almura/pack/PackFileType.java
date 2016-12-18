/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import java.util.Optional;

public enum PackFileType {
    BLOCK,
    ITEM;

    public static Optional<PackFileType> from(String id) {
        for (PackFileType type : values()) {
            if (type.toString().equals(id.toUpperCase())) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }
}
