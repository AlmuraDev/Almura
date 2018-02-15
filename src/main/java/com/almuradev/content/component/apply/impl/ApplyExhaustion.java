/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply.impl;

import com.almuradev.content.component.DoubleRanges;
import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.component.apply.context.ApplyContext;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class ApplyExhaustion implements Apply<EntityPlayer, ApplyContext> {

    public static final ConfigurationNodeDeserializer<ApplyExhaustion> PARSER = config -> DoubleRanges.deserialize(config).map(ApplyExhaustion::new);
    private final DoubleRange exhaustion;

    private ApplyExhaustion(final DoubleRange exhaustion) {
        this.exhaustion = exhaustion;
    }

    @Override
    public boolean accepts(final Entity entity) {
        return entity instanceof EntityPlayer;
    }

    @Override
    public void apply0(final EntityPlayer entity, final ApplyContext context) {
        entity.addExhaustion((float) this.exhaustion.random(context.random()));
    }
}
