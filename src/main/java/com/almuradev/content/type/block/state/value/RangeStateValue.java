/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.state.value;

public interface RangeStateValue<V extends Comparable<V>> extends StateValue<V> {
    Class<V> type();

    V min();

    V max();
}
