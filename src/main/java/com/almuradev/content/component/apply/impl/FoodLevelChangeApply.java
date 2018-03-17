/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply.impl;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.component.apply.context.ItemApplyContext;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.almuradev.toolbox.util.math.IntRange;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class FoodLevelChangeApply implements Apply<EntityPlayer, ItemApplyContext> {
    public static final ConfigurationNodeDeserializer<FoodLevelChangeApply> PARSER = config -> IntRange.PARSER.deserialize(config).map(FoodLevelChangeApply::new);
    private final IntRange change;

    private FoodLevelChangeApply(final IntRange change) {
        this.change = change;
    }

    @Override
    public boolean accepts(final Entity entity) {
        return entity instanceof EntityPlayer;
    }

    @Override
    public void apply0(final EntityPlayer entity, final ItemApplyContext context) {
        final ItemStack usedStack = context.item();

        if (usedStack.getItem() instanceof ItemFood) {
            entity.getFoodStats().addStats((ItemFood) context.item().getItem(), context.item());
        }
    }

    public IntRange getChange() {
        return this.change;
    }
}
