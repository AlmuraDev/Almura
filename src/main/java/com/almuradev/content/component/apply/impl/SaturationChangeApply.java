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
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class SaturationChangeApply implements Apply<EntityPlayer, ApplyContext> {

    public static final ConfigurationNodeDeserializer<SaturationChangeApply> PARSER = config -> DoubleRange.PARSER.deserialize(config).map
            (SaturationChangeApply::new);

    private final DoubleRange change;

    private SaturationChangeApply(final DoubleRange change) {
        this.change = change;
    }

    @Override
    public boolean accepts(Entity entity) {
        return entity instanceof EntityPlayer;
    }

    @Override
    public void apply0(EntityPlayer entity, ApplyContext context) {
        entity.getFoodStats().foodSaturationLevel = (float) this.change.random(context.random());
    }

    public DoubleRange getChange() {
        return this.change;
    }
}
