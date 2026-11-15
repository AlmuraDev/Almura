/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.skills.network.ClientboundSkills;
import com.almuradev.almura.feature.skills.SkillEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.Skills;
import org.inspirenxe.skills.api.event.ChangeExperienceEvent;
import org.inspirenxe.skills.api.function.LevelFunction;
import org.spongepowered.api.ResourceKey;
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

/**
 * The player's skills from the Skills plugin, as 3.5's {@code ServerSkillsManager}: sent when they join (once Skills has loaded their
 * experience), when their experience changes (a few at a time, as it changes with every block broken) and when they change world (skills
 * can differ per world).
 * <p>
 * Only loaded with Sponge and Skills present; see {@link ServerSkills}.
 */
public final class SpongeSkills {

    /** The skills shown to players, in display order, as 3.5's. */
    public static final String[] SKILLS = {"crafting", "digger", "farming", "mining", "woodcutting"};

    // Experience changes on every block broken, so updates wait a little and go together
    private static final int UPDATE_DELAY_TICKS = 10;
    // Skills loads the player's experience after they join
    private static final int JOIN_DELAY_TICKS = 40;

    // Players due their skills, and in how many ticks
    private static final Map<UUID, Integer> pending = new HashMap<>();

    private SpongeSkills() {
    }

    static void install() {
        NeoForge.EVENT_BUS.register(new Hooks());
    }

    /**
     * Sends the player their skills in {@code delayTicks}, unless they are already due them.
     */
    static void schedule(final UUID player, final int delayTicks) {
        pending.putIfAbsent(player, delayTicks);
    }

    /**
     * Sends the player their skills now, if Skills has them.
     */
    public static void send(final ServerPlayer player) {
        final List<SkillEntry> skills = entries(player);
        if (!skills.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new ClientboundSkills(skills));
        }
    }

    private static List<SkillEntry> entries(final ServerPlayer player) {
        final List<SkillEntry> entries = new ArrayList<>();
        if (!Skills.isAvailable()) {
            return entries;
        }
        final SkillService service = Skills.service();
        final Optional<SkillHolder> holder = holder(service, player);
        for (final String id : SKILLS) {
            final SkillType type = service.skillType(id).orElse(null);
            if (type == null) {
                continue;
            }
            final double experience = holder.flatMap(found -> found.skill(type)).map(Skill::experience).orElse(0d);
            final LevelFunction levels = type.levelFunction();
            final int maxLevel = type.maxLevel();
            final int level = Math.max(1, Math.min(maxLevel, levels.levelFor(experience)));
            final double levelExperience = levels.xpFor(level);
            final double nextLevelExperience = level < maxLevel ? levels.xpFor(level + 1) : levelExperience;
            final double maxExperience = levels.xpFor(maxLevel);
            entries.add(new SkillEntry(id, LegacyComponentSerializer.legacySection().serialize(type.formattedName()), color(type.formattedName()),
                level, maxLevel, experience, levelExperience, nextLevelExperience, maxExperience));
        }
        return entries;
    }

    /**
     * The player's skills in the world they are in, or the world it shares skills from (the Nether shares the Overworld's).
     */
    private static Optional<SkillHolder> holder(final SkillService service, final ServerPlayer player) {
        final ResourceKey world = ResourceKey.resolve(player.level().dimension().identifier().toString());
        return service.containerFor(world).flatMap(container -> container.holder(player.getUUID()));
    }

    /**
     * @return the skill's colour: its name's, or its name's first coloured part's
     */
    private static int color(final Component name) {
        TextColor color = name.color();
        if (color == null) {
            for (final Component child : name.children()) {
                if (child.color() != null) {
                    color = child.color();
                    break;
                }
            }
        }
        return color == null ? 0xFFFFFF : color.value();
    }

    /**
     * The NeoForge side: joining, changing world, respawning, leaving, and the server's ticks that send what is due.
     */
    public static final class Hooks {

        @SubscribeEvent
        public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
            schedule(event.getEntity().getUUID(), JOIN_DELAY_TICKS);
        }

        @SubscribeEvent
        public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
            pending.remove(event.getEntity().getUUID());
        }

        @SubscribeEvent
        public void onChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
            schedule(event.getEntity().getUUID(), 1);
        }

        @SubscribeEvent
        public void onRespawn(final PlayerEvent.PlayerRespawnEvent event) {
            schedule(event.getEntity().getUUID(), 1);
        }

        @SubscribeEvent
        public void onServerTick(final ServerTickEvent.Post event) {
            if (pending.isEmpty()) {
                return;
            }
            final MinecraftServer server = event.getServer();
            final Iterator<Map.Entry<UUID, Integer>> due = pending.entrySet().iterator();
            final List<UUID> send = new ArrayList<>();
            while (due.hasNext()) {
                final Map.Entry<UUID, Integer> entry = due.next();
                if (entry.getValue() <= 1) {
                    due.remove();
                    send.add(entry.getKey());
                } else {
                    entry.setValue(entry.getValue() - 1);
                }
            }
            for (final UUID uniqueId : send) {
                final ServerPlayer player = server.getPlayerList().getPlayer(uniqueId);
                if (player != null) {
                    send(player);
                }
            }
        }

        @SubscribeEvent
        public void onServerStarted(final ServerStartedEvent event) {
            final Optional<PluginContainer> almura = Sponge.pluginManager().plugin(Almura.ID);
            if (almura.isEmpty()) {
                Almura.LOGGER.warn("Sponge doesn't know Almura as a plugin; the HUD won't follow skill experience");
                return;
            }
            Sponge.eventManager().registerListeners(almura.get(), new Experience(), MethodHandles.lookup());
            Almura.LOGGER.info("Hooked into Skills: players' skills go to the HUD and the Features menu");
        }
    }

    /**
     * The Skills side: a player's experience changed, so their skills are sent shortly, on the server's thread.
     */
    public static final class Experience {

        @Listener(order = Order.LAST)
        public void onChangeExperience(final ChangeExperienceEvent.Post event) {
            final UUID player = event.skill().holder().uniqueId();
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                server.execute(() -> schedule(player, UPDATE_DELAY_TICKS));
            }
        }
    }
}
