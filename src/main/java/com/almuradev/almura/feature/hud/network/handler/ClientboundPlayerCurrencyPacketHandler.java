/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network.handler;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerCurrencyPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import java.text.DecimalFormat;

import javax.inject.Inject;

public final class ClientboundPlayerCurrencyPacketHandler implements MessageHandler<ClientboundPlayerCurrencyPacket> {

    private final HeadUpDisplay hudData;

    @Inject
    private ClientboundPlayerCurrencyPacketHandler(final HeadUpDisplay hudData) {
        this.hudData = hudData;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundPlayerCurrencyPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {

            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                final DecimalFormat format = new DecimalFormat("###,###.##");

                hudData.isEconomyPresent = true;
                hudData.economyAmount = format.format(message.money);
            }
        }
    }
}
