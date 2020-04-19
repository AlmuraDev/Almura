/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature;

import com.almuradev.almura.shared.feature.filter.Direction;

public class FeatureSortType implements SortType {
    private final String typeId;
    private final String name;
    private final Direction direction;

    public FeatureSortType(final String typeId, final String name, final Direction direction) {
        this.typeId = typeId;
        this.name = name;
        this.direction = direction;
    }

    @Override
    public String getTypeId() {
        return this.typeId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }
}
