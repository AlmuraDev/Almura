/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.component.action.breaks.drop;

import org.spongepowered.api.util.weighted.VariableAmount;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ExperienceDrop extends Drop {

    public ExperienceDrop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance) {
        super(amount, bonusAmount, bonusChance);
    }
}
