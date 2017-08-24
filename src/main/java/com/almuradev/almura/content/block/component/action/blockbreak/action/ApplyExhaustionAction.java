/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.component.action.blockbreak.action;

import com.almuradev.almura.configuration.serializer.ConfigurationNodeDeserializer;
import com.almuradev.almura.content.block.component.action.Action;
import com.almuradev.almura.content.type.VariableAmounts;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Optional;
import java.util.Random;

public final class ApplyExhaustionAction implements Action {

    public static final ConfigurationNodeDeserializer<ApplyExhaustionAction> SERIALIZER = node ->
            Optional.of(new ApplyExhaustionAction(VariableAmounts.SERIALIZER.defaultedDeserialize(node, VariableAmounts.FIXED_1)));
    private final VariableAmount exhaustion;

    private ApplyExhaustionAction(final VariableAmount exhaustion) {
        this.exhaustion = exhaustion;
    }

    @Override
    public void apply(final EntityPlayer player, final Block block, final BlockPos pos, final Random random, final ItemStack stack) {
        player.addExhaustion((float) this.exhaustion.getAmount(random));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("exhaustion", this.exhaustion)
                .toString();
    }
}
