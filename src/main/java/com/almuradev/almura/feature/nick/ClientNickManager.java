/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.nick.asm.mixin.iface.IMixinEntityPlayer;
import com.almuradev.almura.feature.nick.network.ServerboundNicknameOpenRequestPacket;
import com.almuradev.almura.feature.nick.network.ServerboundNucleusNameChangePacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientNickManager implements Witness {

    private final Map<UUID, Text> nicks = new HashMap<>();
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ClientNickManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @SubscribeEvent
    public void onClientConnectToServer(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.nicks.clear();
    }

    @SubscribeEvent
    public void onEntityJoinWorld(final EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
            final Text nick = this.getNicknameFor(event.getEntity().getUniqueID());
            if (nick != null) {
                ((IMixinEntityPlayer) event.getEntity()).setDisplayName(TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick));
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

    @Nullable
    public Text getNicknameFor(UUID uniqueId) {
        return this.nicks.get(uniqueId);
    }

    public void setForgeNickname(final EntityPlayer player, final String nick) {
        ((IMixinEntityPlayer) player).setDisplayName(nick);
    }

    public void requestNicknameGUI() {
        this.network.sendToServer(new ServerboundNicknameOpenRequestPacket());
    }

    public void requestNicknameChange(@Nullable final String nick) {
        this.network.sendToServer(new ServerboundNucleusNameChangePacket(nick));
    }
}
