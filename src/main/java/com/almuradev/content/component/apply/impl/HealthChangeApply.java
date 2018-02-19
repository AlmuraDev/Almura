/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply.impl;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.component.apply.context.ApplyContext;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class HealthChangeApply implements Apply<EntityLivingBase, ApplyContext> {
    public static final ConfigurationNodeDeserializer<HealthChangeApply> PARSER = config -> DoubleRange.PARSER.deserialize(config).map(HealthChangeApply::new);
    private final DoubleRange change;

    private HealthChangeApply(final DoubleRange change) {
        this.change = change;
    }

    @Override
    public boolean accepts(final Entity entity) {
        return entity instanceof EntityLivingBase;
    }

    @Override
    public void apply0(final EntityLivingBase entity, final ApplyContext context) {
        final double heal = this.change.random(context.random());
        entity.heal((float) heal);
    }
}
