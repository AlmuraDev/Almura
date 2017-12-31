/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply.impl;

import com.almuradev.almura.shared.util.VariableAmounts;
import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.component.apply.context.ApplyContext;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.util.weighted.VariableAmount;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class ApplyExhaustion implements Apply<EntityPlayer, ApplyContext> {

    public static final ConfigurationNodeDeserializer<ApplyExhaustion> PARSER = config -> VariableAmounts.deserialize(config).map(ApplyExhaustion::new);
    private final VariableAmount exhaustion;

    private ApplyExhaustion(final VariableAmount exhaustion) {
        this.exhaustion = exhaustion;
    }

    @Override
    public boolean accepts(final Entity entity) {
        return entity instanceof EntityPlayer;
    }

    @Override
    public void apply0(final EntityPlayer entity, final ApplyContext context) {
        entity.addExhaustion((float) this.exhaustion.getAmount(context.random()));
    }
}
