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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientNickManager implements Witness {

    private final Map<UUID, Text> nicks = new HashMap<>();

    private static Field displayNameField;

    static {
        try {
            displayNameField = EntityPlayer.class.getDeclaredField("displayname");
            displayNameField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onClientConnectToServer(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.nicks.clear();
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

    public void adjustPlayerNickname(EntityPlayer player, String nickname) throws IllegalAccessException {
        displayNameField.set(player, nickname);
    }

    @Nullable
    public Text getNicknameFor(UUID uniqueId) {
        return this.nicks.get(uniqueId);
    }
}
