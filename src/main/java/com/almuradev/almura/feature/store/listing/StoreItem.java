/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.listing;

import com.almuradev.almura.shared.item.VanillaStack;

import java.math.BigDecimal;
import java.time.Instant;

public interface StoreItem extends VanillaStack {

    int getRecord();

    Instant getCreated();

    int getIndex();

    void setIndex(final int index);

    BigDecimal getPrice();

    @Override
    StoreItem copy();
}
