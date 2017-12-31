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
import com.almuradev.content.component.apply.context.ItemApplyContext;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Random;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class ReduceDurability implements Apply<EntityPlayer, ItemApplyContext> {

    public static final ConfigurationNodeDeserializer<ReduceDurability> PARSER = config -> VariableAmounts.deserialize(config).map(ReduceDurability::new);
    private final VariableAmount reduction;

    private ReduceDurability(final VariableAmount reduction) {
        this.reduction = reduction;
    }

    @Override
    public boolean accepts(final Entity entity) {
        return entity instanceof EntityPlayerMP;
    }

    @Override
    public void apply0(final EntityPlayer entity, final ItemApplyContext context) {
        if (entity instanceof EntityPlayerMP) {
            final Random random = context.random();
            context.item().attemptDamageItem(this.reduction.getFlooredAmount(random), random, (EntityPlayerMP) entity);
        }
    }
}
