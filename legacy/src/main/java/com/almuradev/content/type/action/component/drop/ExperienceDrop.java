/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

import com.almuradev.toolbox.util.math.DoubleRange;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ExperienceDrop extends Drop {

    static final String EXPERIENCE = "experience";

    public ExperienceDrop(final DoubleRange amount, @Nullable final DoubleRange bonusAmount, @Nullable final DoubleRange bonusChance) {
        super(amount, bonusAmount, bonusChance);
    }
}
