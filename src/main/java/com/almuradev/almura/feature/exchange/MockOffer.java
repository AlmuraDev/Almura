package com.almuradev.almura.feature.exchange;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.item.inventory.ItemStack;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class MockOffer {
    public final Instant instant;
    public final ItemStack item;
    public final BigDecimal pricePer;
    public final UUID playerUuid;
    public final String playerName;

    public MockOffer(final ItemStack item, final EntityPlayer player) {
        this(Instant.now(), item, BigDecimal.valueOf(0), player.getUniqueID(), player.getName());
    }

    public MockOffer(final Instant instant, final ItemStack item, final BigDecimal pricePer, final UUID playerUuid, final String playerName) {
        this.instant = instant;
        this.item = item;
        this.pricePer = pricePer;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }
}