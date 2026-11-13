/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.complex.block.coinexchange.CoinExchange;
import com.almuradev.almura.feature.complex.block.membershipexchange.MembershipExchange;
import net.minecraft.util.ResourceLocation;

public class ComplexBlocks {
    public static ComplexBlock COIN_EXCHANGE = new CoinExchange(new ResourceLocation(Almura.ID, "machine/coin_exchange"), 3F, 1000F);
    public static ComplexBlock MEMBERSHIP_EXCHANGE = new MembershipExchange(new ResourceLocation(Almura.ID, "machine/membership_exchange"), 3F, 1000F);
}
