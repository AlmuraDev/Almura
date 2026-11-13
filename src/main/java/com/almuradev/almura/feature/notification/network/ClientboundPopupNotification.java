/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A popup notification for the player's HUD: its title and message (with colour codes) and how many seconds it shows.
 */
public record ClientboundPopupNotification(String title, String message, int secondsToLive) implements CustomPacketPayload {

    public static final Type<ClientboundPopupNotification> TYPE = AlmuraNetwork.type("popup_notification");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPopupNotification> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ClientboundPopupNotification::title,
        ByteBufCodecs.STRING_UTF8, ClientboundPopupNotification::message,
        ByteBufCodecs.VAR_INT, ClientboundPopupNotification::secondsToLive,
        ClientboundPopupNotification::new);

    @Override
    public Type<ClientboundPopupNotification> type() {
        return TYPE;
    }
}
