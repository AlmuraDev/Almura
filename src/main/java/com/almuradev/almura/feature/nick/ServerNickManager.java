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
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("deprecation")
@Singleton
public final class ServerNickManager extends Witness.Impl implements Witness.Lifecycle {

    private final PluginContainer container;
    private final Game game;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    private ServerNickManager(final PluginContainer container, final Game game, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.container = container;
        this.game = game;
        this.network = network;
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event) {
        this.game.getServiceManager().provide(NucleusNicknameService.class).ifPresent(service -> {
            service.getNickname(event.getTargetEntity()).ifPresent(nick -> {
                final String oldNick = TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick);

                // Tell the Forge mods
                final String newNick = ForgeEventFactory.getPlayerDisplayName((EntityPlayer) event.getTargetEntity(), TextSerializers
                        .LEGACY_FORMATTING_CODE.serialize(nick));

                if (!oldNick.equals(newNick)) {
                    try {
                        this.setNickname(service, event.getTargetEntity(), TextSerializers.LEGACY_FORMATTING_CODE.deserialize(newNick));
                    } catch (NicknameException e) {
                        e.printStackTrace();
                        return;
                    }
                    this.setForgeNickname((EntityPlayer) event.getTargetEntity(), newNick);
                } else {
                    this.setForgeNickname((EntityPlayer) event.getTargetEntity(), newNick);
                }
            });

            this.sendNicknameUpdate(service, event.getTargetEntity());
        });
    }

    @Listener(order = Order.LAST)
    public void onMoveEntityTeleportPlayer(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
        if (this.differentExtent(event.getFromTransform(), event.getToTransform())) {
            this.game.getServiceManager().provide(NucleusNicknameService.class).ifPresent(service -> {
                service.getNickname(player).ifPresent(nick -> {
                    final String oldNick = TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick);

                    // Tell the Forge mods
                    final String newNick = ForgeEventFactory.getPlayerDisplayName((EntityPlayer) event.getTargetEntity(), TextSerializers
                            .LEGACY_FORMATTING_CODE.serialize(nick));

                    if (!oldNick.equals(newNick)) {
                        try {
                            this.setNickname(service, player, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(newNick));
                        } catch (NicknameException e) {
                            e.printStackTrace();
                            return;
                        }
                        this.setForgeNickname((EntityPlayer) player, newNick);
                    } else {
                        this.setForgeNickname((EntityPlayer) player, newNick);
                    }
                });

                this.sendNicknameUpdate(service, player);
            });
        }
    }

    @Listener(order = Order.POST)
    public void onChangeNickname(final NucleusChangeNicknameEvent event, @Getter("getTargetUser") final Player player) {
        this.game.getServiceManager().provide(NucleusNicknameService.class).ifPresent(service -> {

            final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;
            final String oldNick = mcPlayer.getDisplayNameString();
            final String newNick = TextSerializers.LEGACY_FORMATTING_CODE.serialize(event.getNewNickname());

            if (!oldNick.equals(newNick)) {
                // Tell the Forge mods
                final String modNick = ForgeEventFactory.getPlayerDisplayName(mcPlayer, newNick);
                final Text finalNick = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(modNick);

                if (!newNick.equals(modNick)) {
                    try {
                        this.setNickname(service, player, finalNick);
                    } catch (NicknameException e) {
                        e.printStackTrace();
                        return;
                    }
                    this.setForgeNickname((EntityPlayer) player, modNick);
                } else {
                    this.setForgeNickname((EntityPlayer) player, modNick);
                }

                this.network.sendToAll(this.getMappingMessage(player, this.getFormattedNickname(service, player)));
            }
        });
    }

    public void setNickname(NucleusNicknameService service, final Player player, final Text nick) throws NicknameException {
        service.setNickname(player, nick);
    }

    public void removeNickname(NucleusNicknameService service, final Player player) throws NicknameException {
        service.removeNickname(player);
    }

    public void setForgeNickname(final EntityPlayer player, final String nick) {
        ((IMixinEntityPlayer) player).setDisplayName(nick);
    }

    private Text getFormattedNickname(NucleusNicknameService service, Player player) {
        Text nickname = service.getNickname(player).orElse(null);
        if (nickname == null) {
            nickname = Text.of(player.getName());
        } else {
            nickname = Text.of("~", nickname);
        }

        return nickname;
    }

    public String getNickname(Player player) {
        // Todo: Zidane please put this in a functional lamda
        Optional<NucleusNicknameService> service = Sponge.getServiceManager().provide(NucleusNicknameService.class);
        if (service.isPresent()) {
            return this.getFormattedNickname(service.get(), player).toPlain();
        }
        return player.getName(); //Default return value when Nucleus isn't loaded.
    }

    public void sendNicknameUpdate(NucleusNicknameService service, Player player) {
        final ClientboundNucleusNameChangeMappingPacket joiningPlayerPacket = this.getMappingMessage(player, this.getFormattedNickname(service, player));

        this.game.getServer().getOnlinePlayers().stream().filter(onlinePlayer -> !onlinePlayer.getUniqueId().equals(player.getUniqueId()))
                .forEach(onlinePlayer -> this.network.sendTo(onlinePlayer, joiningPlayerPacket));

        Task.builder()
                .delayTicks(40)
                .execute(t -> this.network.sendTo(player, this.getMappingMessage(service)))
                .submit(this.container);
    }

    private ClientboundNucleusNameChangeMappingPacket getMappingMessage(final Player player, final Text nick) {
        return new ClientboundNucleusNameChangeMappingPacket(player.getUniqueId(), nick);
    }

    private ClientboundNucleusNameMappingsPacket getMappingMessage(NucleusNicknameService service) {
        final Map<UUID, Text> nicknames = new HashMap<>();
        this.game.getServer().getOnlinePlayers().forEach(player -> {
            final Text nickname = this.getFormattedNickname(service, player);

            nicknames.put(player.getUniqueId(), nickname);
        });

        return new ClientboundNucleusNameMappingsPacket(nicknames);
    }

    private boolean differentExtent(final Transform<World> from, final Transform<World> to) {
        return !from.getExtent().equals(to.getExtent());
    }
}
