/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.data.blockbreak.drop;

import org.spongepowered.api.util.weighted.VariableAmount;

import javax.annotation.Nullable;

public final class ExperienceDrop extends Drop {

    public ExperienceDrop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance) {
        super(amount, bonusAmount, bonusChance);
    }
}
