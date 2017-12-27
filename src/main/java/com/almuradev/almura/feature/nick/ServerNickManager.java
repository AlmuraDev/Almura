/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.nucleuspowered.nucleus.api.events.NucleusChangeNicknameEvent;
import io.github.nucleuspowered.nucleus.api.exceptions.NicknameException;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import net.kyori.membrane.facet.Activatable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.common.text.SpongeTexts;

import java.util.stream.Collectors;

@Singleton
public final class ServerNickManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

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
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event) {
        this.game.getServiceManager().provide(NucleusNicknameService.class).ifPresent((service) -> {
            service.getNickname(event.getTargetEntity()).ifPresent((nick) -> {
                // Trigger Forge event so mod's know of the nickname
                ((EntityPlayerMP) event.getTargetEntity()).refreshDisplayName();
            });

            Task.builder().async().delayTicks(40).execute(t -> {
                // Send everyone's nicknames to the joining player
                this.network.sendTo(event.getTargetEntity(), this.getMappingMessage(service, event.getTargetEntity()));
            }).submit(this.container);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerNameFormat(final PlayerEvent.NameFormat event) {
        final EntityPlayer player = event.getEntityPlayer();

        this.game.getServiceManager().provide(NucleusNicknameService.class).ifPresent((service) -> service.getNickname((User) (Object) player)
                .ifPresent((nick) -> event.setDisplayname(TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick))));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerNameFormatPost(final PlayerEvent.NameFormat event) {
        final EntityPlayer player = event.getEntityPlayer();

        this.game.getServiceManager().provide(NucleusNicknameService.class).ifPresent((service) -> {
            final Text oldNick = service.getNickname((Player) event.getEntityPlayer()).orElse(Text.of(player.getName()));
            final Text newNick = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(event.getDisplayname());

            // Update Nucleus
            if (!oldNick.equals(newNick)) {
                try {
                    service.setNickname((User) player, newNick);
                } catch (final NicknameException e) {
                    e.printStackTrace();
                }
            } else {
                // Schedule client update
                Task.builder().async().delayTicks(40).execute(t -> this.network.sendToAll(this.getMappingMessage((Player) player, newNick)))
                        .submit(this.container);
            }
        });
    }

    @Listener(order = Order.POST)
    public void onPlayerChangeNickname(final NucleusChangeNicknameEvent event, @Getter("getTargetUser") final Player player) {
        final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;
        final ITextComponent mcOldText = mcPlayer.getDisplayName();
        final ITextComponent mcNewText = SpongeTexts.toComponent(event.getNewNickname());

        if (!mcOldText.equals(mcNewText)) {
            Task.builder().delayTicks(1).execute(((EntityPlayerMP) player)::refreshDisplayName).submit(this.container);
        }
    }

    private ClientboundNucleusNameChangeMappingPacket getMappingMessage(final Player player, final Text nick) {
        return new ClientboundNucleusNameChangeMappingPacket(player.getUniqueId(), nick);
    }

    private ClientboundNucleusNameMappingsPacket getMappingMessage(final NucleusNicknameService service, final Player toIgnore) {
        return new ClientboundNucleusNameMappingsPacket(
                this.game.getServer().getOnlinePlayers().stream().filter((player) -> !player.getUniqueId().equals(toIgnore.getUniqueId())).collect
                        (Collectors.toMap(Identifiable::getUniqueId, v -> service.getNickname(v).orElseGet(() -> Text.of(v.getName()))
                ))
        );
    }
}
