/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The player's money for the HUD, as 1.12 sent it: their balance in the server's economy's default currency, and that currency's symbol.
 * Sent only by a server with an economy (Sponge's, such as EconomyLite's).
 */
public record ClientboundPlayerMoney(String symbol, double balance) implements CustomPacketPayload {

    public static final Type<ClientboundPlayerMoney> TYPE = AlmuraNetwork.type("player_money");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerMoney> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ClientboundPlayerMoney::symbol,
        ByteBufCodecs.DOUBLE, ClientboundPlayerMoney::balance,
        ClientboundPlayerMoney::new);

    @Override
    public Type<ClientboundPlayerMoney> type() {
        return TYPE;
    }
}
