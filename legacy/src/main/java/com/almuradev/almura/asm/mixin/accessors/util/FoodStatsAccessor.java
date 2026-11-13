/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.util;

import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodStats.class)
public interface FoodStatsAccessor {
    //public net.minecraft.util.FoodStats field_75125_b # foodSaturationLevel  (float)
    @Accessor("foodSaturationLevel") float accessor$getFoodSaturationLevel();
    @Accessor("foodSaturationLevel") void accessor$setFoodSaturationLevel(float level);

    //public net.minecraft.util.FoodStats field_75127_a # foodLevel
    @Accessor("foodLevel") int accessor$getFoodLevel();
    @Accessor("foodLevel") void accessor$setFoodLevel(int value);
}
