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
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Random;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class ReduceDurability implements Apply {

    public static final ConfigurationNodeDeserializer<ReduceDurability> PARSER = config -> VariableAmounts.deserialize(config).map(ReduceDurability::new);
    private final VariableAmount reduction;

    private ReduceDurability(final VariableAmount reduction) {
        this.reduction = reduction;
    }

    @Override
    public void apply(final EntityPlayer player, final Block block, final BlockPos pos, final Random random, final ItemStack stack) {
        if (player instanceof EntityPlayerMP) {
            stack.attemptDamageItem(this.reduction.getFlooredAmount(random), random, (EntityPlayerMP) player);
        }
    }
}
