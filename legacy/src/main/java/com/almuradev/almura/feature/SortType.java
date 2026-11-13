/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature;

import com.almuradev.almura.shared.feature.filter.Direction;

public interface SortType {
    String getTypeId();
    String getName();
    Direction getDirection();
}