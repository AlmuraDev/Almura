/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

import org.spongepowered.api.util.weighted.VariableAmount;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ExperienceDrop extends Drop {

    static final String EXPERIENCE = "experience";

    public ExperienceDrop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance) {
        super(amount, bonusAmount, bonusChance);
    }
}
