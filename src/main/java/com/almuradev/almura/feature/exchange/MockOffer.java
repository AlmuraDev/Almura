/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.item.inventory.ItemStack;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.UUID;

public final class MockOffer {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    public final Instant instant;
    public final ItemStack item;
    public final BigDecimal pricePer;
    public final String playerName;
    public final int slotId;
    private final UUID playerUuid;
    public long quantity;
    public boolean listed;

    public MockOffer(final int slotId, final ItemStack item, final EntityPlayer player) {
        this(Instant.now(), slotId, item, item.getQuantity(), BigDecimal.valueOf(0), player.getUniqueID(), player.getName());
    }

    public MockOffer(final Instant instant, final int slotId, final ItemStack item, final long quantity, final BigDecimal pricePer,
        final UUID playerUuid, final String playerName) {
        this.instant = instant;
        this.slotId = slotId;
        this.item = item;
        this.quantity = quantity;
        this.pricePer = pricePer;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("timestamp", this.instant)
            .add("item", this.item)
            .add("price", FORMAT.format(this.pricePer))
            .add("player", this.playerName + "(" + this.playerUuid + ")")
            .toString();
    }
}