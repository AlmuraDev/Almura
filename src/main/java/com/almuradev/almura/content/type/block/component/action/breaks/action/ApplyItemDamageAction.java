/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.breaks.action;

import com.almuradev.almura.configuration.serializer.ConfigurationNodeDeserializer;
import com.almuradev.almura.content.type.VariableAmounts;
import com.almuradev.almura.content.type.block.component.action.Action;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Optional;
import java.util.Random;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class ApplyItemDamageAction implements Action {

    public static final ConfigurationNodeDeserializer<ApplyItemDamageAction> SERIALIZER = node ->
            Optional.of(new ApplyItemDamageAction(VariableAmounts.deserialize(node, VariableAmounts.FIXED_1)));
    private final VariableAmount damage;

    private ApplyItemDamageAction(final VariableAmount damage) {
        this.damage = damage;
    }

    @Override
    public void apply(final EntityPlayer player, final Block block, final BlockPos pos, final Random random, final ItemStack stack) {
        stack.damageItem(this.damage.getFlooredAmount(random), player);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("damage", this.damage)
                .toString();
    }
}
