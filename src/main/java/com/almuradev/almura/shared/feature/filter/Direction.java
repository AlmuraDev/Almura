/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.filter;

import java.util.Optional;

public enum Direction {
    ASCENDING("asc"),
    DESCENDING("desc");

    private String id;
    Direction(final String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

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
