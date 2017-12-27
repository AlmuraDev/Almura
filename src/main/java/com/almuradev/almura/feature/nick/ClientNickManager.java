/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.text.SpongeTexts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientNickManager implements Witness {

    private final Map<UUID, Text> nicks = new HashMap<>();

    @SubscribeEvent
    public void onClientConnectToServer(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.nicks.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerNameFormat(final PlayerEvent.NameFormat event) {
        final EntityPlayer player = event.getEntityPlayer();

        final Text nick = this.nicks.get(player.getUniqueID());
        if (nick != null) {
            event.setDisplayname(TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerNameFormatPost(final PlayerEvent.NameFormat event) {
        final EntityPlayer player = event.getEntityPlayer();

        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.connection != null) {
            final NetworkPlayerInfo info = Minecraft.getMinecraft().player.connection.getPlayerInfo(player.getUniqueID());
            if (info != null) {
                info.setDisplayName(SpongeTexts.toComponent(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(event.getDisplayname())));
            }
        }
    }

    public void putAll(final Map<UUID, Text> nicksById) {
        checkNotNull(nicksById);

        this.nicks.putAll(nicksById);
    }

    public void put(final UUID uniqueId, final Text nick) {
        checkNotNull(uniqueId);
        checkNotNull(nick);

        this.nicks.put(uniqueId, nick);
    }
}
