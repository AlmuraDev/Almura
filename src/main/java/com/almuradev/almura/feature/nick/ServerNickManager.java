/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import com.almuradev.almura.feature.nick.asm.mixin.iface.IMixinEntityPlayer;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.nucleuspowered.nucleus.api.events.NucleusChangeNicknameEvent;
import io.github.nucleuspowered.nucleus.api.exceptions.NicknameException;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("deprecation")
@Singleton
public final class ServerNickManager extends Witness.Impl implements Witness.Lifecycle {

    private final PluginContainer container;
    private final Scheduler scheduler;
    private final ServiceManager serviceManager;
    private final ChannelBinding.IndexedMessageChannel network;
    private NucleusChangeNicknameEvent.Post event;
    private Player player;

    @Inject
    private ServerNickManager(final PluginContainer container, final Scheduler scheduler, final ServiceManager serviceManager, @ChannelId
        (NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.container = container;
        this.scheduler = scheduler;
        this.serviceManager = serviceManager;
        this.network = network;
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        this.scheduler
            .createTaskBuilder()
            .delayTicks(20)
            .execute(() -> this.serviceManager.provide(NucleusNicknameService.class).ifPresent(service -> {
                service.getNickname(player).ifPresent(nick -> {
                    final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;

                    final String oldNick = TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick);

                    final String newNick = ForgeEventFactory.getPlayerDisplayName(mcPlayer, oldNick);

                    if (!oldNick.equals(newNick)) {
                        try {
                            this.setNickname(service, player, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(newNick));
                        } catch (NicknameException e) {
                            e.printStackTrace();
                            return;
                        }
                        this.setForgeNickname(mcPlayer, newNick);
                    }
                });

                this.sendNicknameUpdate(service, player);
            }))
            .submit(this.container);
    }

    @Listener(order = Order.LAST)
    public void onMoveEntityTeleportPlayer(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
        if (!event.getFromTransform().getExtent().getUniqueId().equals(event.getToTransform().getExtent().getUniqueId())) {
            this.scheduler
                .createTaskBuilder()
                .delayTicks(20)
                .execute(() -> this.serviceManager.provide(NucleusNicknameService.class).ifPresent(service -> {
                    service.getNickname(player).ifPresent(nick -> {
                        final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;

                        final String oldNick = TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick);

                        final String newNick = ForgeEventFactory.getPlayerDisplayName(mcPlayer, oldNick);

                        if (!oldNick.equals(newNick)) {
                            try {
                                this.setNickname(service, player, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(newNick));
                            } catch (NicknameException e) {
                                e.printStackTrace();
                                return;
                            }
                            this.setForgeNickname(mcPlayer, newNick);
                        }
                    });

                    this.sendNicknameUpdate(service, player);
                }))
                .submit(this.container);
        }
    }

    @Listener(order = Order.POST)
    public void onChangeNickname(final NucleusChangeNicknameEvent.Pre event, @Getter("getTargetUser") final Player player) {
        //System.out.println("Nickname Change Event fired");
        this.scheduler
            .createTaskBuilder()
            .delayTicks(1) // This is a "Pre" event, we want to get the value one tick later
            .execute(() -> this.serviceManager.provide(NucleusNicknameService.class).ifPresent(service -> {

                final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;
                final String oldNick = mcPlayer.getDisplayNameString();
                final String newNick = TextSerializers.LEGACY_FORMATTING_CODE.serialize(event.getNewNickname());

                //System.out.println("Player: " + player + " old: " + oldNick + " newNick: " + newNick);

                if (!oldNick.equals(newNick)) {
                    final String modNick = ForgeEventFactory.getPlayerDisplayName(mcPlayer, newNick);
                    final Text finalNick = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(modNick);
                    //System.out.println("Pass 1");
                    if (!newNick.equals(modNick)) {
                        //System.out.println("Pass 2");
                        try {
                            //System.out.println("Should have set nickname");
                            this.setNickname(service, player, finalNick);
                        } catch (NicknameException e) {
                            e.printStackTrace();
                            return;
                        }
                        //System.out.println("Set Forge name");
                        this.setForgeNickname(mcPlayer, modNick);
                    }

                    //System.out.println("Sending new updates to: " + player);
                    this.sendNicknameUpdate(service, player);
                }
            }))
            .submit(this.container);
    }

    public void setNickname(final NucleusNicknameService service, final Player player, final Text nick) throws NicknameException {
        service.setNickname(player, nick);
    }

    public void removeNickname(final NucleusNicknameService service, final Player player) throws NicknameException {
        service.removeNickname(player);
    }

    public void setForgeNickname(final EntityPlayer player, final String nick) {
        ((IMixinEntityPlayer) player).setDisplayName(nick);
    }

    public String getNickname(final Player player) {
        Optional<NucleusNicknameService> service = Sponge.getServiceManager().provide(NucleusNicknameService.class);
        //Default return value when Nucleus isn't loaded.
        return service.map(nucleusNicknameService -> this.getFormattedNickname(nucleusNicknameService, player).toPlain()).orElseGet(player::getName);
    }

    public void sendNicknameUpdate(final NucleusNicknameService service, final Player player) {
        final ClientboundNucleusNameChangeMappingPacket packet = this.getMappingMessage(player, this.getFormattedNickname(service, player));

        Sponge.getServer().getOnlinePlayers().stream().filter(onlinePlayer -> !onlinePlayer.getUniqueId().equals(player.getUniqueId()))
            .forEach(onlinePlayer -> this.network.sendTo(onlinePlayer, packet));

        this.network.sendTo(player, this.getMappingMessage(service));
    }

    private ClientboundNucleusNameChangeMappingPacket getMappingMessage(final Player player, final Text nick) {
        return new ClientboundNucleusNameChangeMappingPacket(player.getUniqueId(), nick);
    }

    private ClientboundNucleusNameMappingsPacket getMappingMessage(final NucleusNicknameService service) {
        final Map<UUID, Text> nicknames = new HashMap<>();
        
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            final Text nickname = this.getFormattedNickname(service, player);

            nicknames.put(player.getUniqueId(), nickname);
        });

        return new ClientboundNucleusNameMappingsPacket(nicknames);
    }

    private Text getFormattedNickname(final NucleusNicknameService service, final Player player) {
        Text nickname = service.getNickname(player).orElse(null);
        if (nickname == null) {
            nickname = Text.of(player.getName());
        } else {
            nickname = Text.of("~", nickname);
        }

        return nickname;
    }
}
