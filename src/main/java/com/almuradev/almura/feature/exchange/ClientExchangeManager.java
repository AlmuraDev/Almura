/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.feature.exchange.network.ServerboundExchangeOpenRequestPacket;
import com.almuradev.almura.shared.client.keyboard.binder.KeyBindingEntry;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SideOnly(Side.CLIENT)
public final class ClientExchangeManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final KeyBinding exchangeOpenBinding;

    @Inject
    public ClientExchangeManager(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network, final Set<KeyBindingEntry>
            keybindings) {
        this.network = network;
        this.exchangeOpenBinding = keybindings.stream().map(KeyBindingEntry::getKeybinding).filter((keyBinding -> keyBinding
                .getKeyDescription().equalsIgnoreCase("key.almura.exchange.open"))).findFirst().orElse(null);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (exchangeOpenBinding.isPressed()) {
            requestExchangeGUI();
        }
    }

    public void requestExchangeGUI() {
        this.network.sendToServer(new ServerboundExchangeOpenRequestPacket());
    }
}
