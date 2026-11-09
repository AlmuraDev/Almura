/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills;

import com.almuradev.almura.feature.membership.network.ClientboundMembershipGuiOpenPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import org.inspirenxe.skills.api.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import javax.inject.Inject;

/**
 * Skill levels for other features. Always available, so no method here may name a Skills type in its signature: without the Skills plugin that
 * would fail as soon as this class is inspected. Every method returns a negative value when Skills isn't installed.
 */
public final class SkillsHandler implements Witness {

    public static final String PLUGIN_ID = "skills";

    private final ServerNotificationManager serverNotificationManager;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public SkillsHandler(final ServerNotificationManager serverNotificationManager, final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.serverNotificationManager = serverNotificationManager;
        this.network = network;
    }

    public double getExperienceForSkill(Player player, String skillName) {
        if (!Sponge.getPluginManager().isLoaded(PLUGIN_ID)) {
            return -1;
        }
        final SkillService service = Sponge.getServiceManager().provide(SkillService.class).orElse(null);
        if (service == null) {
            return -1;
        }
        final SkillType skillType = Sponge.getRegistry().getType(SkillType.class, "skills:"+ skillName).orElse(null);
        if (skillType == null) {
            return -2;
        }

        SkillHolderContainer container = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (container != null) {
            container = service.getParentContainer(container).orElse(container);
            SkillHolder holder = container.getHolder(player.getUniqueId()).orElse(null);
            if (holder != null) {
                final Skill skill = holder.getSkill(skillType).orElse(null);
                if (skill != null) {
                    return skill.getCurrentExperience();
                }
            }
        }
        return -3;
    }

    public int getLevelForSkill(Player player, String skillName) {
        if (!Sponge.getPluginManager().isLoaded(PLUGIN_ID)) {
            return -1;
        }
        final SkillService service = Sponge.getServiceManager().provide(SkillService.class).orElse(null);
        if (service == null) {
            return -1;
        }
        final SkillType skillType = Sponge.getRegistry().getType(SkillType.class, "skills:" + skillName).orElse(null);
        if (skillType == null) {
            return -2;
        }
        SkillHolderContainer container = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (container != null) {
            container = service.getParentContainer(container).orElse(container);
            SkillHolder holder = container.getHolder(player.getUniqueId()).orElse(null);
            if (holder != null) {
                final Skill skill = holder.getSkill(skillType).orElse(null);
                if (skill != null) {
                    return skill.getCurrentLevel();
                }
            }
        }
        return -1;
    }

    public int getTotalSkillLevel(Player player) {
        if (!Sponge.getPluginManager().isLoaded(PLUGIN_ID)) {
            return -1;
        }
        int total = 0;
        boolean missing = false;
        for (final String skillName : ServerSkillsManager.SKILLS) {
            final int level = getLevelForSkill(player, skillName);
            if (level == -2) {
                return -2;
            }
            if (level <= -1) {
                missing = true;
            }
            total += level;
        }
        return missing ? -1 : total;
    }
}
