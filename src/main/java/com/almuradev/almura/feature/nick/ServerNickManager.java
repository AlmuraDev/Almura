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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
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

import java.lang.reflect.Field;
import java.util.stream.Collectors;

@Singleton
public final class ServerNickManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final PluginContainer container;
    private final Game game;
    private final ChannelBinding.IndexedMessageChannel network;

    private static Field displayNameField;

    static {
        try {
            displayNameField = EntityPlayer.class.getDeclaredField("displayname");
            displayNameField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

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
                final String oldNick = TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick);

                // Tell the Forge mods
                final String newNick = ForgeEventFactory.getPlayerDisplayName((EntityPlayer) event.getTargetEntity(), TextSerializers
                        .LEGACY_FORMATTING_CODE.serialize(nick));

                if (!oldNick.equals(newNick)) {
                    try {
                        service.setNickname(event.getTargetEntity(), TextSerializers.LEGACY_FORMATTING_CODE.deserialize(newNick));
                    } catch (NicknameException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    displayNameField.set(event.getTargetEntity(), newNick);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            // Send the joining player's nickname to others
            final ClientboundNucleusNameChangeMappingPacket joiningPlayerPacket =
                    this.getMappingMessage(event.getTargetEntity(), service.getNickname(event.getTargetEntity()).orElse(Text.of(event
                            .getTargetEntity().getName())));

            this.game.getServer().getOnlinePlayers().stream().filter((player) -> !player.getUniqueId().equals(event.getTargetEntity().getUniqueId()))
                    .forEach((player) -> this.network.sendTo(player, joiningPlayerPacket));

            Task.builder()
                    .delayTicks(40)
                    .execute(t -> {
                        // Send everyone's nicknames to the joining player
                        this.network.sendTo(event.getTargetEntity(), this.getMappingMessage(service));
                    })
                    .submit(this.container);
        });
    }

    @Listener(order = Order.POST)
    public void onChangeNickname(final NucleusChangeNicknameEvent event, @Getter("getTargetUser") final Player player) throws IllegalAccessException {
        final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;
        final String oldNick = mcPlayer.getDisplayNameString();
        final String newNick = TextSerializers.LEGACY_FORMATTING_CODE.serialize(event.getNewNickname());

        if (!oldNick.equals(newNick)) {
            // Tell the Forge mods
            final String modNick = ForgeEventFactory.getPlayerDisplayName(mcPlayer, newNick);
            final Text finalNick = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(modNick);

            if (!newNick.equals(modNick)) {

                // Mod says our nick needs to be something else
                this.game.getServiceManager().provide(NucleusNicknameService.class).ifPresent((service) -> {
                    try {
                        service.setNickname(player, finalNick);
                    } catch (NicknameException e) {
                        e.printStackTrace();
                    }
                });
            }

            displayNameField.set(mcPlayer, modNick);

            // Tell everyone about the new nick
            this.network.sendToAll(this.getMappingMessage(player, finalNick));
        }
    }

    private ClientboundNucleusNameChangeMappingPacket getMappingMessage(final Player player, final Text nick) {
        return new ClientboundNucleusNameChangeMappingPacket(player.getUniqueId(), nick);
    }

    private ClientboundNucleusNameMappingsPacket getMappingMessage(final NucleusNicknameService service) {
        return new ClientboundNucleusNameMappingsPacket(
                this.game.getServer().getOnlinePlayers().stream().collect(Collectors.toMap(Identifiable::getUniqueId, v -> service.getNickname(v)
                        .orElseGet(() -> Text.of(v.getName()))
                ))
        );
    }
}
