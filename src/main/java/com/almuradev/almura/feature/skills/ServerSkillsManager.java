/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills;

import com.almuradev.almura.feature.skills.network.ClientboundSkillsPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.kyori.membrane.facet.Activatable;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ChangeExperienceEvent;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Keeps each player's client up to date with their skill levels and experience.
 */
public final class ServerSkillsManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    /** The skills shown to players, in display order. */
    public static final String[] SKILLS = { "crafting", "digger", "farming", "mining", "woodcutting" };

    // Experience changes on every block broken, so updates are batched per player
    private static final int UPDATE_DELAY_TICKS = 10;
    private static final int JOIN_DELAY_TICKS = 40;

    private final Game game;
    private final PluginContainer container;
    private final ChannelBinding.IndexedMessageChannel network;
    private final Set<UUID> pendingUpdates = new HashSet<>();

    @Inject
    private ServerSkillsManager(final Game game, final PluginContainer container,
            @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.game = game;
        this.container = container;
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

    @Listener(order = Order.LAST)
    public void onClientJoin(final ClientConnectionEvent.Join event) {
        // The Skills plugin loads the player's experience after they join
        this.scheduleUpdate(event.getTargetEntity(), JOIN_DELAY_TICKS);
    }

    @Listener(order = Order.LAST)
    public void onClientDisconnect(final ClientConnectionEvent.Disconnect event) {
        this.pendingUpdates.remove(event.getTargetEntity().getUniqueId());
    }

    @Listener(order = Order.LAST)
    public void onTeleport(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
        // Skill containers can differ per world
        if (!event.getFromTransform().getExtent().getUniqueId().equals(event.getToTransform().getExtent().getUniqueId())) {
            this.scheduleUpdate(player, 1);
        }
    }

    @Listener(order = Order.LAST)
    public void onRespawn(final RespawnPlayerEvent event) {
        if (!event.getFromTransform().getExtent().getUniqueId().equals(event.getToTransform().getExtent().getUniqueId())) {
            this.scheduleUpdate(event.getTargetEntity(), 1);
        }
    }

    @Listener(order = Order.LAST)
    public void onChangeExperience(final ChangeExperienceEvent.Post event) {
        this.game.getServer().getPlayer(event.getHolderId()).ifPresent(player -> this.scheduleUpdate(player, UPDATE_DELAY_TICKS));
    }

    private void scheduleUpdate(final Player player, final int delayTicks) {
        final UUID uniqueId = player.getUniqueId();
        if (!this.pendingUpdates.add(uniqueId)) {
            return; // An update is already on its way
        }

        Task.builder()
                .delayTicks(delayTicks)
                .execute(() -> {
                    this.pendingUpdates.remove(uniqueId);
                    this.game.getServer().getPlayer(uniqueId).ifPresent(this::sendSkills);
                })
                .submit(this.container);
    }

    public void sendSkills(final Player player) {
        final List<SkillEntry> skills = this.createEntries(player);
        if (!skills.isEmpty()) {
            this.network.sendTo(player, new ClientboundSkillsPacket(skills));
        }
    }

    private List<SkillEntry> createEntries(final Player player) {
        final List<SkillEntry> entries = new ArrayList<>();
        final SkillService service = this.game.getServiceManager().provide(SkillService.class).orElse(null);
        if (service == null) {
            return entries;
        }

        final SkillHolder holder = this.getHolder(service, player).orElse(null);

        for (final String id : SKILLS) {
            final SkillType type = this.game.getRegistry().getType(SkillType.class, "skills:" + id).orElse(null);
            if (type == null) {
                continue;
            }

            final double experience = holder == null ? 0 : holder.getSkill(type).map(Skill::getCurrentExperience).orElse(0d);
            final LevelFunctionType levels = type.getLevelFunction();
            final int maxLevel = type.getMaxLevel();
            final int level = Math.max(1, Math.min(maxLevel, levels.getLevelFor(experience)));
            final double levelExperience = levels.getXPFor(level);
            final double nextLevelExperience = level < maxLevel ? levels.getXPFor(level + 1) : levelExperience;
            final double maxExperience = levels.getXPFor(maxLevel);

            entries.add(new SkillEntry(id, TextSerializers.LEGACY_FORMATTING_CODE.serialize(type.getFormattedName()), this.getColor(type), level,
                    maxLevel, experience, levelExperience, nextLevelExperience, maxExperience));
        }
        return entries;
    }

    private Optional<SkillHolder> getHolder(final SkillService service, final Player player) {
        SkillHolderContainer holderContainer = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (holderContainer == null) {
            return Optional.empty();
        }
        holderContainer = service.getParentContainer(holderContainer).orElse(holderContainer);
        return holderContainer.getHolder(player.getUniqueId());
    }

    private int getColor(final SkillType type) {
        // The color may be on the name itself or on its first colored child
        for (final Text text : type.getFormattedName().withChildren()) {
            final TextColor color = text.getColor();
            if (color != TextColors.NONE && color != TextColors.RESET) {
                return color.getColor().getRgb();
            }
        }
        return 0xFFFFFF;
    }
}
