/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.permission.RuntimePermissions;
import com.almuradev.almura.feature.nick.network.NickMessages.ClientboundNames;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.module.nickname.NucleusNicknameService;
import io.github.nucleuspowered.nucleus.api.module.nickname.event.NucleusChangeNicknameEvent;
import io.github.nucleuspowered.nucleus.api.module.nickname.exception.NicknameException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.plugin.PluginContainer;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Nicknames from Nucleus, as 3.5's {@code ServerNickManager} and its name packets: a player joining (or changing world) is sent everyone's
 * nickname and everyone theirs, a nickname Nucleus changes (its {@code /nick} too) goes to everyone, and the Nicknames window's changes go to
 * Nucleus.
 * <p>
 * Only loaded with Sponge and Nucleus present; see {@link ServerNicks}.
 */
final class SpongeNicks {

    // 3.5's NickUtil: letters, digits, _ and colour codes
    private static final Pattern ALLOWED = Pattern.compile("[a-zA-Z0-9_§]+");
    // 3.5's: Nucleus loads the player's data after they join
    private static final int JOIN_DELAY_TICKS = 20;
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    // Players due everyone's nicknames, and in how many ticks
    private static final Map<UUID, Integer> pending = new HashMap<>();

    private SpongeNicks() {
    }

    static void install() {
        NeoForge.EVENT_BUS.register(new Hooks());
    }

    static boolean available() {
        return service().isPresent();
    }

    private static Optional<NucleusNicknameService> service() {
        try {
            return NucleusAPI.getNicknameService();
        } catch (final RuntimeException e) {
            // Nucleus not started, or its nickname module off
            return Optional.empty();
        }
    }

    /**
     * Sets or removes the player's nickname through Nucleus, with 3.5's rules: letters, digits and _ (and colour codes), 4 to 20 of them,
     * and Nucleus' own {@code nucleus.nick.base}.
     */
    static void change(final ServerPlayer player, final String raw) {
        final NucleusNicknameService service = service().orElse(null);
        if (service == null) {
            return;
        }
        final String name = player.getGameProfile().name();
        final boolean removing = raw.isBlank() || raw.trim().equalsIgnoreCase(name);
        final String nickname = raw.trim();
        if (!removing) {
            if (!RuntimePermissions.has(player, "nucleus.nick.base")) {
                ServerNotifications.sendPopup(player, "Nickname Error!", "Insufficient Permissions!", 5);
                return;
            }
            final String plain = nickname.replaceAll("§.", "");
            if (!ALLOWED.matcher(nickname).matches()) {
                ServerNotifications.sendPopup(player, "Nickname Error!", "Invalid Character in Nickname!", 5);
                return;
            }
            if (plain.length() <= 3 || plain.length() > 20) {
                ServerNotifications.sendPopup(player, "Nickname Error!", "Validation of Nickname data failed!", 5);
                return;
            }
        }
        try {
            if (removing) {
                service.removeNickname(player.getUUID());
            } else {
                service.setNickname(player.getUUID(), LEGACY.deserialize(nickname));
            }
        } catch (final NicknameException e) {
            ServerNotifications.sendPopup(player, "Nickname Error!", e.getMessage() == null ? "Nucleus refused the nickname." : e.getMessage(), 5);
            return;
        }
        Almura.LOGGER.info("{} {}", name, removing ? "removed their nickname" : "set their nickname to '" + nickname + "'");
        broadcast(player.getUUID());
        // As 3.5: the player, and everyone else, told
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (final ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (online == player) {
                ServerNotifications.sendPopup(online, removing ? "Nickname Removed!" : "New Nickname!", "You are now known as "
                    + (removing ? name : nickname), 5);
            } else {
                ServerNotifications.sendPopup(online, removing ? "Nickname removed!" : "New Nickname!", removing
                    ? name + " has removed their nickname." : name + " is now known as " + nickname, 5);
            }
        }
    }

    /**
     * @return the player's nickname as 3.5 showed it ({@code ~} and the nickname, colour codes as section signs), or empty for none
     */
    private static String nickname(final NucleusNicknameService service, final UUID player) {
        return service.getNickname(player).map(nick -> "~" + LEGACY.serialize(nick)).orElse("");
    }

    /**
     * Sends everyone the player's nickname.
     */
    static void broadcast(final UUID player) {
        service().ifPresent(service -> {
            final Map<UUID, String> change = new HashMap<>();
            change.put(player, nickname(service, player));
            PacketDistributor.sendToAllPlayers(new ClientboundNames(false, change));
        });
    }

    /**
     * Sends the player everyone's nickname, and everyone theirs.
     */
    static void sendAll(final ServerPlayer player) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        final NucleusNicknameService service = service().orElse(null);
        if (server == null || service == null) {
            return;
        }
        final Map<UUID, String> nicknames = new HashMap<>();
        for (final ServerPlayer online : server.getPlayerList().getPlayers()) {
            final String nickname = nickname(service, online.getUUID());
            if (!nickname.isEmpty()) {
                nicknames.put(online.getUUID(), nickname);
            }
        }
        PacketDistributor.sendToPlayer(player, new ClientboundNames(true, nicknames));
        broadcast(player.getUUID());
    }

    /**
     * The NeoForge side: joining and changing world, and the server's ticks that send what is due.
     */
    public static final class Hooks {

        @SubscribeEvent
        public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
            pending.put(event.getEntity().getUUID(), JOIN_DELAY_TICKS);
        }

        @SubscribeEvent
        public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
            pending.remove(event.getEntity().getUUID());
        }

        @SubscribeEvent
        public void onChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
            pending.putIfAbsent(event.getEntity().getUUID(), JOIN_DELAY_TICKS);
        }

        @SubscribeEvent
        public void onServerTick(final ServerTickEvent.Post event) {
            if (pending.isEmpty()) {
                return;
            }
            final List<UUID> due = new ArrayList<>();
            final Iterator<Map.Entry<UUID, Integer>> entries = pending.entrySet().iterator();
            while (entries.hasNext()) {
                final Map.Entry<UUID, Integer> entry = entries.next();
                if (entry.getValue() <= 1) {
                    entries.remove();
                    due.add(entry.getKey());
                } else {
                    entry.setValue(entry.getValue() - 1);
                }
            }
            for (final UUID uniqueId : due) {
                final ServerPlayer player = event.getServer().getPlayerList().getPlayer(uniqueId);
                if (player != null) {
                    sendAll(player);
                }
            }
        }

        @SubscribeEvent
        public void onServerStarted(final ServerStartedEvent event) {
            final Optional<PluginContainer> almura = Sponge.pluginManager().plugin(Almura.ID);
            if (almura.isEmpty()) {
                Almura.LOGGER.warn("Sponge doesn't know Almura as a plugin; name tags won't follow Nucleus' nickname changes");
                return;
            }
            Sponge.eventManager().registerListeners(almura.get(), new Changes(), MethodHandles.lookup());
            Almura.LOGGER.info("Hooked into Nucleus: players' nicknames go to their name tags");
        }
    }

    /**
     * The Nucleus side: a nickname changed (its {@code /nick} too), so everyone is sent it, on the server's thread.
     */
    public static final class Changes {

        @Listener(order = Order.LAST)
        public void onChangeNickname(final NucleusChangeNicknameEvent.Post event) {
            final UUID player = event.getUser();
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                server.execute(() -> broadcast(player));
            }
        }
    }
}
