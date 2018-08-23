/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.filter;

import java.util.Optional;

public enum Direction {
    ASCENDING,
    DESCENDING;

    public static Optional<Direction> getDirection(final String id) {
        Direction direction = null;

        switch (id.toUpperCase()) {
            case "ASC":
                direction = ASCENDING;
                break;
            case "DESC":
                direction = DESCENDING;
                break;
        }

        return Optional.ofNullable(direction);
    }
}
